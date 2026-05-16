package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.RezervacijaResponse;
import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.model.Rezervacija;
import hr.beatsync.backend.repository.RezervacijaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rezervacije")
public class RezervacijaController {

    private final RezervacijaRepository rezervacijaRepo;

    public RezervacijaController(RezervacijaRepository rezervacijaRepo) {
        this.rezervacijaRepo = rezervacijaRepo;
    }

    @GetMapping
    public ResponseEntity<List<RezervacijaResponse>> list() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        List<Rezervacija> lista = isBusiness
                ? rezervacijaRepo.findByBusinessRezervacija_UsernameBusiness(username)
                : rezervacijaRepo.findByIzvodacRezervacija_UsernameIzvodac(username);

        // Auto IN_PROGRESS: prebaci ACCEPTED rezervacije na datum eventa
        LocalDate danas = LocalDate.now();
        LocalTime sada = LocalTime.now();
        boolean anyUpdated = false;
        for (Rezervacija r : lista) {
            if (r.getStatusRezervacije() == StatusRezervacije.ACCEPTED
                    && r.getJobOffer() != null
                    && !danas.isBefore(r.getJobOffer().getDatum())
                    && !sada.isBefore(r.getJobOffer().getPocetak())) {
                r.setStatusRezervacije(StatusRezervacije.IN_PROGRESS);
                anyUpdated = true;
            }
        }
        if (anyUpdated) {
            rezervacijaRepo.saveAll(lista);
        }

        return ResponseEntity.ok(lista.stream().map(RezervacijaResponse::from).toList());
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
            }
        }

        rezervacijaRepo.save(rez);
        return ResponseEntity.ok(RezervacijaResponse.from(rez));
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
        return ResponseEntity.ok(RezervacijaResponse.from(rez));
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
        return ResponseEntity.ok(RezervacijaResponse.from(rez));
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
        return ResponseEntity.ok(RezervacijaResponse.from(rez));
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
