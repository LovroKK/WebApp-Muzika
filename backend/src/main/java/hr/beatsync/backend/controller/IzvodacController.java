package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.IzvodacProfilResponse;
import hr.beatsync.backend.dto.UpdateIzvodacRequest;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/izvodac")
public class IzvodacController {

    private final IzvodacKorisnikRepository izvodacRepo;

    public IzvodacController(IzvodacKorisnikRepository izvodacRepo) {
        this.izvodacRepo = izvodacRepo;
    }

    @GetMapping("/profil")
    public ResponseEntity<IzvodacProfilResponse> getProfil() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        IzvodacKorisnik izvodac = izvodacRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));
        return ResponseEntity.ok(IzvodacProfilResponse.from(izvodac));
    }

    @PutMapping("/profil")
    public ResponseEntity<IzvodacProfilResponse> updateProfil(@Valid @RequestBody UpdateIzvodacRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        IzvodacKorisnik izvodac = izvodacRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));

        izvodac.setIme(request.getIme());
        izvodac.setPrezime(request.getPrezime());
        izvodac.setEmail(request.getEmail());
        izvodac.setBrojTelefona(request.getBrojTelefona());
        izvodac.setLinkMixtape(request.getLinkMixtape());
        izvodac.setCijenaPoSatu(request.getCijenaPoSatu());
        izvodac.setKratkiOpis(request.getKratkiOpis());
        izvodac.setPrijasnjiPoslovi(request.getPrijasnjiPoslovi());
        izvodac.setRadiOd(request.getRadiOd());
        izvodac.setUkupnoGodinaIskustva(request.getUkupnoGodinaIskustva());

        izvodacRepo.save(izvodac);
        return ResponseEntity.ok(IzvodacProfilResponse.from(izvodac));
    }
}
