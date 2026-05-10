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
        boolean isBusiness = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));

        List<Rezervacija> lista = isBusiness
                ? rezervacijaRepo.findByBusinessRezervacija_UsernameBusiness(username)
                : rezervacijaRepo.findByIzvodacRezervacija_UsernameIzvodac(username);

        return ResponseEntity.ok(lista.stream().map(RezervacijaResponse::from).toList());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> promijeniStatus(@PathVariable Integer id, @RequestParam String status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isBusiness = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));

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

        rez.setStatusRezervacije(noviStatus);
        if (noviStatus == StatusRezervacije.ACCEPTED) {
            rez.setPotvrdaRezervacije(true);
        }

        rezervacijaRepo.save(rez);
        return ResponseEntity.ok(RezervacijaResponse.from(rez));
    }
}
