package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.RezervacijaResponse;
import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.enums.VrstaPosiljatelja;
import hr.beatsync.backend.model.Poruka;
import hr.beatsync.backend.model.Rezervacija;
import hr.beatsync.backend.repository.JobOfferRepository;
import hr.beatsync.backend.repository.PlacanjeRepository;
import hr.beatsync.backend.repository.PorukaRepository;
import hr.beatsync.backend.repository.RezervacijaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rezervacije")
public class RezervacijaController {

    private final RezervacijaRepository rezervacijaRepo;
    private final JobOfferRepository jobOfferRepo;
    private final PorukaRepository porukaRepo;
    private final PlacanjeRepository placanjeRepo;

    public RezervacijaController(RezervacijaRepository rezervacijaRepo,
                                 JobOfferRepository jobOfferRepo,
                                 PorukaRepository porukaRepo,
                                 PlacanjeRepository placanjeRepo) {
        this.rezervacijaRepo = rezervacijaRepo;
        this.jobOfferRepo = jobOfferRepo;
        this.porukaRepo = porukaRepo;
        this.placanjeRepo = placanjeRepo;
    }

    private RezervacijaResponse toResponse(Rezervacija r) {
        RezervacijaResponse dto = RezervacijaResponse.from(r);
        placanjeRepo.findTopByRezervacija_IdRezervacijeOrderByDatumKreiranjaDesc(r.getIdRezervacije())
                .ifPresent(p -> dto.setPaymentStatus(p.getStatusPlacanja().name()));
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<RezervacijaResponse>> list() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        List<Rezervacija> lista = isBusiness
                ? rezervacijaRepo.findByBusinessRezervacija_UsernameBusiness(username)
                : rezervacijaRepo.findByIzvodacRezervacija_UsernameIzvodac(username);

        return ResponseEntity.ok(lista.stream().map(this::toResponse).toList());
    }

    // Potvrdi suradnju — oba korisnika
    @PutMapping("/{id}/potvrdi")
    public ResponseEntity<?> potvrdi(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        Rezervacija rez = getAndCheckAccess(id, username, isBusiness);

        if (rez.getStatusRezervacije() != StatusRezervacije.REQUESTED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Potvrda nije moguća u statusu: " + rez.getStatusRezervacije()));
        }

        if (isBusiness) {
            rez.setPotvrdaBusiness(true);
        } else {
            rez.setPotvrdaIzvodac(true);
            // Ako je business već potvrdio → ACCEPTED
            if (Boolean.TRUE.equals(rez.getPotvrdaBusiness())) {
                rez.setStatusRezervacije(StatusRezervacije.ACCEPTED);
                rez.setPotvrdaRezervacije(true);
                // Označi job offer kao popunjen — nestaje s liste ponuda
                if (rez.getJobOffer() != null) {
                    rez.getJobOffer().setPopunjen(true);
                    jobOfferRepo.save(rez.getJobOffer());
                }
            }
        }

        rezervacijaRepo.save(rez);
        return ResponseEntity.ok(toResponse(rez));
    }

    // Otkaži / odbij — oba korisnika mogu otkazati REQUESTED
    @PutMapping("/{id}/otkazi")
    public ResponseEntity<?> otkazi(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        Rezervacija rez = getAndCheckAccess(id, username, isBusiness);

        if (rez.getStatusRezervacije() != StatusRezervacije.REQUESTED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Otkazivanje nije moguće u statusu: " + rez.getStatusRezervacije()));
        }

        rez.setStatusRezervacije(StatusRezervacije.CANCELLED);
        rezervacijaRepo.save(rez);

        // Označi sve nepročitane poruke te rezervacije kao pročitane za stranu koja odbija
        List<Poruka> neprocitane = isBusiness
                ? porukaRepo.findUnreadByRezervacijaForBusiness(rez.getIdRezervacije())
                : porukaRepo.findUnreadByRezervacijaForIzvodac(rez.getIdRezervacije());
        if (!neprocitane.isEmpty()) {
            neprocitane.forEach(p -> p.setReadStatus(true));
            porukaRepo.saveAll(neprocitane);
        }

        // Pošalji SYSTEM_NOTIFICATION izvođaču kada BUSINESS odbija
        if (isBusiness) {
            String nazivPonude = rez.getJobOffer() != null ? rez.getJobOffer().getNazivPonude() : "ponudu";
            Poruka notif = Poruka.builder()
                    .sadrzajPoruke("Vaša prijava za ponudu \"" + nazivPonude + "\" je odbijena.")
                    .posiljatelj(VrstaPosiljatelja.BUSINESS)
                    .izvodacPoruka(rez.getIzvodacRezervacija())
                    .businessPoruka(rez.getBusinessRezervacija())
                    .idRezervacije(rez.getIdRezervacije())
                    .messageType("SYSTEM_NOTIFICATION")
                    .readStatus(false)
                    .timestampPoruke(LocalDateTime.now())
                    .build();
            porukaRepo.save(notif);
        }

        return ResponseEntity.ok(toResponse(rez));
    }

    // Završi — samo BUSINESS, IN_PROGRESS → COMPLETED
    @PutMapping("/{id}/zavrsi")
    public ResponseEntity<?> zavrsi(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        if (!isBusiness) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Samo business korisnici mogu označiti rezervaciju završenom"));
        }

        Rezervacija rez = getAndCheckAccess(id, username, true);

        if (rez.getStatusRezervacije() != StatusRezervacije.IN_PROGRESS) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Završetak nije moguć u statusu: " + rez.getStatusRezervacije()));
        }

        rez.setStatusRezervacije(StatusRezervacije.COMPLETED);
        rezervacijaRepo.save(rez);
        return ResponseEntity.ok(toResponse(rez));
    }

    // Stari endpoint — zadržan za kompatibilnost, ali ACCEPTED se sada radi kroz /potvrdi
    @PutMapping("/{id}/status")
    public ResponseEntity<?> promijeniStatus(@PathVariable Integer id, @RequestParam String status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isBusiness = isBusiness(auth);

        if (!isBusiness) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Samo business korisnici mogu mijenjati status"));
        }

        Rezervacija rez = rezervacijaRepo.findByIdRezervacije(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervacija ne postoji"));

        if (!rez.getBusinessRezervacija().getUsernameBusiness().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nemate pristup ovoj rezervaciji"));
        }

        StatusRezervacije noviStatus;
        try {
            noviStatus = StatusRezervacije.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nepoznati status: " + status);
        }

        // ACCEPTED se ne može postaviti direktno — koristite /potvrdi endpoint
        if (noviStatus == StatusRezervacije.ACCEPTED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Koristite /potvrdi endpoint za prihvat — potrebna dvostruka potvrda"));
        }

        rez.setStatusRezervacije(noviStatus);
        rezervacijaRepo.save(rez);
        return ResponseEntity.ok(toResponse(rez));
    }

    private boolean isBusiness(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));
    }

    private Rezervacija getAndCheckAccess(Integer id, String username, boolean isBusiness) {
        Rezervacija rez = rezervacijaRepo.findByIdRezervacije(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervacija ne postoji"));

        boolean hasAccess = isBusiness
                ? rez.getBusinessRezervacija().getUsernameBusiness().equals(username)
                : rez.getIzvodacRezervacija().getUsernameIzvodac().equals(username);

        if (!hasAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pristup ovoj rezervaciji");
        }
        return rez;
    }
}
