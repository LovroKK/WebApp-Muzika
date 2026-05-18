package hr.beatsync.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import hr.beatsync.backend.dto.CheckoutSessionResponse;
import hr.beatsync.backend.dto.CreateCheckoutSessionRequest;
import hr.beatsync.backend.enums.StatusPlacanja;
import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.model.Placanje;
import hr.beatsync.backend.model.Rezervacija;
import hr.beatsync.backend.repository.PlacanjeRepository;
import hr.beatsync.backend.repository.RezervacijaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PlacanjeService {

    private static final Logger log = LoggerFactory.getLogger(PlacanjeService.class);

    private final PlacanjeRepository placanjeRepo;
    private final RezervacijaRepository rezervacijaRepo;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    public PlacanjeService(PlacanjeRepository placanjeRepo,
                           RezervacijaRepository rezervacijaRepo) {
        this.placanjeRepo = placanjeRepo;
        this.rezervacijaRepo = rezervacijaRepo;
    }

    @Transactional
    public CheckoutSessionResponse createCheckoutSession(String businessUsername,
                                                         CreateCheckoutSessionRequest req) {
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Stripe nije konfiguriran — postavi stripe.secret.key u application.properties");
        }

        Rezervacija rez = rezervacijaRepo.findByIdRezervacije(req.getIdRezervacije())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervacija ne postoji"));

        if (!rez.getBusinessRezervacija().getUsernameBusiness().equals(businessUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pristup ovoj rezervaciji");
        }

        if (rez.getStatusRezervacije() != StatusRezervacije.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Plaćanje je moguće tek nakon završene rezervacije");
        }

        if (placanjeRepo.existsByRezervacija_IdRezervacijeAndStatusPlacanja(
                rez.getIdRezervacije(), StatusPlacanja.PAID)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Plaćanje za ovu rezervaciju je već izvršeno");
        }

        if (rez.getJobOffer() == null || rez.getJobOffer().getBudzet() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Iznos plaćanja nije definiran (job_offer.budzet je null)");
        }

        BigDecimal iznos = rez.getJobOffer().getBudzet().setScale(2, RoundingMode.HALF_UP);
        long iznosCents = iznos.multiply(BigDecimal.valueOf(100)).longValueExact();

        String nazivPonude = rez.getJobOffer().getNazivPonude() != null
                ? rez.getJobOffer().getNazivPonude()
                : "Booking #" + rez.getIdRezervacije();

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .setClientReferenceId(String.valueOf(rez.getIdRezervacije()))
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount(iznosCents)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Booking #" + rez.getIdRezervacije() + " – " + nazivPonude)
                                            .setDescription("Izvođač: @" + rez.getIzvodacRezervacija().getUsernameIzvodac())
                                            .build())
                                    .build())
                            .build())
                    .build();

            Session session = Session.create(params);

            Placanje placanje = Placanje.builder()
                    .rezervacija(rez)
                    .iznos(iznos)
                    .currency("eur")
                    .statusPlacanja(StatusPlacanja.PENDING)
                    .stripeSessionId(session.getId())
                    .datumKreiranja(LocalDateTime.now())
                    .build();
            placanjeRepo.save(placanje);

            log.info("Stripe Checkout Session kreirana: {} za rezervaciju #{}", session.getId(), rez.getIdRezervacije());
            return new CheckoutSessionResponse(session.getUrl(), session.getId());
        } catch (StripeException e) {
            log.error("Stripe greška pri kreiranju sessiona", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Greška pri komunikaciji sa Stripe-om: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhookEvent(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook stigao, ali stripe.webhook.secret nije postavljen — odbacujem");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook secret nije konfiguriran");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Webhook signature ne odgovara: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signature");
        }

        log.info("Stripe webhook event: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed" -> handleSessionCompleted(event);
            case "checkout.session.expired" -> handleSessionStatus(event, StatusPlacanja.EXPIRED);
            case "payment_intent.payment_failed" -> handleSessionStatus(event, StatusPlacanja.FAILED);
            default -> log.debug("Ignoriram event tipa {}", event.getType());
        }
    }

    private Optional<Session> extractSession(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> obj = deserializer.getObject();

        if (obj.isEmpty()) {
            log.warn("Deserializer.getObject() prazan (vjerojatno API version mismatch — event API {}, SDK očekuje drugu). Pokušavam deserializeUnsafe().",
                    event.getApiVersion());
            try {
                obj = Optional.of(deserializer.deserializeUnsafe());
            } catch (EventDataObjectDeserializationException e) {
                log.error("deserializeUnsafe() neuspješan: {}", e.getMessage());
                return Optional.empty();
            }
        }

        if (obj.get() instanceof Session session) {
            log.info("Event session id: {}, payment_intent: {}", session.getId(), session.getPaymentIntent());
            return Optional.of(session);
        }

        log.warn("Event payload nije Session, već: {}", obj.get().getClass().getSimpleName());
        return Optional.empty();
    }

    private void handleSessionCompleted(Event event) {
        Optional<Session> sessionOpt = extractSession(event);
        if (sessionOpt.isEmpty()) {
            log.warn("checkout.session.completed — ne mogu izvući Session objekt");
            return;
        }
        Session session = sessionOpt.get();

        Placanje placanje = placanjeRepo.findByStripeSessionId(session.getId()).orElse(null);
        if (placanje == null) {
            log.warn("Placanje nije pronađeno u bazi za session {}", session.getId());
            return;
        }

        if (placanje.getStatusPlacanja() == StatusPlacanja.PAID) {
            log.info("Placanje {} već PAID, idempotent retry", placanje.getIdPlacanja());
            return;
        }

        placanje.setStatusPlacanja(StatusPlacanja.PAID);
        placanje.setDatumPlacanja(LocalDateTime.now());
        placanje.setStripePaymentIntentId(session.getPaymentIntent());
        placanjeRepo.save(placanje);
        log.info("Placanje {} → PAID (rezervacija #{})", placanje.getIdPlacanja(),
                placanje.getRezervacija().getIdRezervacije());
    }

    private void handleSessionStatus(Event event, StatusPlacanja noviStatus) {
        Optional<Session> sessionOpt = extractSession(event);
        if (sessionOpt.isEmpty()) return;
        Session session = sessionOpt.get();

        placanjeRepo.findByStripeSessionId(session.getId()).ifPresent(p -> {
            if (p.getStatusPlacanja() != StatusPlacanja.PAID) {
                p.setStatusPlacanja(noviStatus);
                placanjeRepo.save(p);
                log.info("Placanje {} → {}", p.getIdPlacanja(), noviStatus);
            }
        });
    }

    public Optional<Placanje> findBySession(String sessionId, String businessUsername) {
        return placanjeRepo.findByStripeSessionId(sessionId)
                .filter(p -> p.getRezervacija().getBusinessRezervacija()
                        .getUsernameBusiness().equals(businessUsername));
    }

    public Optional<Placanje> findLatestForRezervacija(Integer idRezervacije, String businessUsername) {
        return placanjeRepo.findTopByRezervacija_IdRezervacijeOrderByDatumKreiranjaDesc(idRezervacije)
                .filter(p -> p.getRezervacija().getBusinessRezervacija()
                        .getUsernameBusiness().equals(businessUsername));
    }
}
