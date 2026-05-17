package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.CreateJobOfferRequest;
import hr.beatsync.backend.dto.JobOfferResponse;
import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.enums.VrstaPosiljatelja;
import hr.beatsync.backend.model.BusinessKorisnik;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.model.JobOffer;
import hr.beatsync.backend.model.Poruka;
import hr.beatsync.backend.model.Rezervacija;
import hr.beatsync.backend.repository.BusinessKorisnikRepository;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import hr.beatsync.backend.repository.JobOfferRepository;
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
@RequestMapping("/api/job-offers")
public class JobOfferController {

    private final JobOfferRepository jobOfferRepo;
    private final BusinessKorisnikRepository businessRepo;
    private final IzvodacKorisnikRepository izvodacRepo;
    private final RezervacijaRepository rezervacijaRepo;
    private final PorukaRepository porukaRepo;

    public JobOfferController(JobOfferRepository jobOfferRepo,
                              BusinessKorisnikRepository businessRepo,
                              IzvodacKorisnikRepository izvodacRepo,
                              RezervacijaRepository rezervacijaRepo,
                              PorukaRepository porukaRepo) {
        this.jobOfferRepo = jobOfferRepo;
        this.businessRepo = businessRepo;
        this.izvodacRepo = izvodacRepo;
        this.rezervacijaRepo = rezervacijaRepo;
        this.porukaRepo = porukaRepo;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateJobOfferRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isBusiness = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));
        if (!isBusiness) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Samo business korisnici mogu objaviti ponudu"));
        }

        String username = auth.getName();
        BusinessKorisnik business = businessRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business korisnik nije pronađen"));

        JobOffer offer = JobOffer.builder()
                .nazivPonude(req.getNazivPonude())
                .datum(req.getDatum())
                .pocetak(req.getPocetak())
                .kraj(req.getKraj())
                .lokacija(req.getLokacija())
                .budzet(req.getBudzet())
                .opisPosla(req.getOpisPosla())
                .potrebnoIskustvo(req.getPotrebnoIskustvo())
                .businessPonuda(business)
                .build();

        jobOfferRepo.save(offer);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(offer, false));
    }

    @GetMapping
    public ResponseEntity<List<JobOfferResponse>> list() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));

        if (isBusiness) {
            List<JobOfferResponse> offers = jobOfferRepo.findByBusinessPonuda_UsernameBusiness(username)
                    .stream()
                    .map(o -> toResponseBusiness(o))
                    .toList();
            return ResponseEntity.ok(offers);
        } else {
            List<StatusRezervacije> aktivni = List.of(
                    StatusRezervacije.REQUESTED,
                    StatusRezervacije.ACCEPTED,
                    StatusRezervacije.IN_PROGRESS
            );
            List<JobOfferResponse> offers = jobOfferRepo.findByPopunjenFalse()
                    .stream()
                    .map(o -> toResponse(o, rezervacijaRepo
                            .existsByJobOffer_IdPonudeAndIzvodacRezervacija_UsernameIzvodacAndStatusRezervacijeIn(
                                    o.getIdPonude(), username, aktivni)))
                    .toList();
            return ResponseEntity.ok(offers);
        }
    }

    @PostMapping("/{id}/prijava")
    public ResponseEntity<?> prijava(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isIzvodac = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_IZVODAC"));
        if (!isIzvodac) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Samo izvođači se mogu prijaviti na ponudu"));
        }

        String username = auth.getName();
        JobOffer offer = jobOfferRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ponuda ne postoji"));

        List<StatusRezervacije> aktivni = List.of(
                StatusRezervacije.REQUESTED,
                StatusRezervacije.ACCEPTED,
                StatusRezervacije.IN_PROGRESS
        );
        if (rezervacijaRepo.existsByJobOffer_IdPonudeAndIzvodacRezervacija_UsernameIzvodacAndStatusRezervacijeIn(id, username, aktivni)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Već ste prijavljeni na ovu ponudu");
        }

        IzvodacKorisnik izvodac = izvodacRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Izvođač nije pronađen"));

        LocalDateTime periodOd = offer.getDatum().atTime(offer.getPocetak());
        LocalDateTime periodDo = offer.getDatum().atTime(offer.getKraj());
        if (periodDo.isBefore(periodOd)) {
            periodDo = periodDo.plusDays(1);
        }

        Rezervacija rez = Rezervacija.builder()
                .jobOffer(offer)
                .periodOd(periodOd)
                .periodDo(periodDo)
                .statusRezervacije(StatusRezervacije.REQUESTED)
                .potvrdaRezervacije(false)
                .potvrdaIzvodac(false)
                .potvrdaBusiness(false)
                .izvodacRezervacija(izvodac)
                .businessRezervacija(offer.getBusinessPonuda())
                .build();

        rezervacijaRepo.save(rez);

        // Auto-kreira SYSTEM_NOTIFICATION za business
        Poruka notif = Poruka.builder()
                .sadrzajPoruke(izvodac.getIme() + " " + izvodac.getPrezime()
                        + " se prijavio/la na ponudu \"" + offer.getNazivPonude() + "\".")
                .posiljatelj(VrstaPosiljatelja.IZVODAC)
                .izvodacPoruka(izvodac)
                .businessPoruka(offer.getBusinessPonuda())
                .idRezervacije(rez.getIdRezervacije())
                .messageType("SYSTEM_NOTIFICATION")
                .readStatus(false)
                .timestampPoruke(LocalDateTime.now())
                .build();
        porukaRepo.save(notif);

        return ResponseEntity.ok(Map.of("poruka", "Uspješno ste se prijavili na ponudu"));
    }

    private JobOfferResponse toResponse(JobOffer o, boolean jeliPrijavljen) {
        return new JobOfferResponse(
                o.getIdPonude(),
                o.getNazivPonude(),
                o.getDatum(),
                o.getPocetak(),
                o.getKraj(),
                o.getLokacija(),
                o.getBudzet(),
                o.getOpisPosla(),
                o.getPotrebnoIskustvo(),
                o.getBusinessPonuda().getUsernameBusiness(),
                o.getBusinessPonuda().getNazivKluba(),
                jeliPrijavljen,
                0
        );
    }

    private JobOfferResponse toResponseBusiness(JobOffer o) {
        int brojPrijava = (int) rezervacijaRepo.countByJobOffer_IdPonudeAndStatusRezervacije(
                o.getIdPonude(), StatusRezervacije.REQUESTED);
        return new JobOfferResponse(
                o.getIdPonude(),
                o.getNazivPonude(),
                o.getDatum(),
                o.getPocetak(),
                o.getKraj(),
                o.getLokacija(),
                o.getBudzet(),
                o.getOpisPosla(),
                o.getPotrebnoIskustvo(),
                o.getBusinessPonuda().getUsernameBusiness(),
                o.getBusinessPonuda().getNazivKluba(),
                false,
                brojPrijava
        );
    }
}
