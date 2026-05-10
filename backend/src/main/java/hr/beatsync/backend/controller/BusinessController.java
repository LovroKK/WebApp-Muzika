package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.BusinessProfilResponse;
import hr.beatsync.backend.dto.UpdateBusinessRequest;
import hr.beatsync.backend.model.BusinessKorisnik;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.repository.BusinessKorisnikRepository;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessKorisnikRepository businessRepo;
    private final IzvodacKorisnikRepository izvodacRepo;

    public BusinessController(BusinessKorisnikRepository businessRepo,
                               IzvodacKorisnikRepository izvodacRepo) {
        this.businessRepo = businessRepo;
        this.izvodacRepo = izvodacRepo;
    }

    @GetMapping("/profil")
    public ResponseEntity<BusinessProfilResponse> getProfil() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BusinessKorisnik business = businessRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));
        return ResponseEntity.ok(BusinessProfilResponse.from(business));
    }

    @PutMapping("/profil")
    public ResponseEntity<BusinessProfilResponse> updateProfil(@Valid @RequestBody UpdateBusinessRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BusinessKorisnik business = businessRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));

        business.setNazivKluba(request.getNazivKluba());
        business.setLokacija(request.getLokacija());
        business.setOpis(request.getOpis());
        business.setEmail(request.getEmail());
        business.setBrojTelefona(request.getBrojTelefona());
        business.setNajboljiIzvodaci(request.getNajboljiIzvodaci());

        if (request.getResidentDjUsername() != null && !request.getResidentDjUsername().isBlank()) {
            IzvodacKorisnik dj = izvodacRepo.findById(request.getResidentDjUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Izvođač ne postoji"));
            business.setResidentDj(dj);
        } else {
            business.setResidentDj(null);
        }

        businessRepo.save(business);
        return ResponseEntity.ok(BusinessProfilResponse.from(business));
    }
}
