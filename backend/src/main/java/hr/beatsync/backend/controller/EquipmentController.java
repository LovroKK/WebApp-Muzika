package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.CreateEquipmentRequest;
import hr.beatsync.backend.dto.EquipmentResponse;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.model.Oprema;
import hr.beatsync.backend.model.OpremaLokacije;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import hr.beatsync.backend.repository.OpremaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final OpremaRepository opremaRepo;
    private final IzvodacKorisnikRepository izvodacRepo;

    public EquipmentController(OpremaRepository opremaRepo,
                               IzvodacKorisnikRepository izvodacRepo) {
        this.opremaRepo = opremaRepo;
        this.izvodacRepo = izvodacRepo;
    }

    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> list() {
        List<EquipmentResponse> equipment = opremaRepo.findAllWithDetailsOrderByIdOpremeDesc()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(equipment);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateEquipmentRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isIzvodac = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_IZVODAC"));

        if (!isIzvodac) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Samo izvođači mogu objaviti opremu"));
        }

        IzvodacKorisnik vlasnik = izvodacRepo.findById(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Izvođač nije pronađen"));

        Oprema oprema = Oprema.builder()
                .nazivOpreme(req.getNazivOpreme().trim())
                .cijena(req.getCijena())
                .kategorija(req.getKategorija())
                .slika(normalizeSlika(req.getSlika()))
                .vlasnikOpreme(vlasnik)
                .build();

        List<OpremaLokacije> lokacije = req.getLokacije().stream()
                .map(String::trim)
                .filter(lokacija -> !lokacija.isBlank())
                .map(lokacija -> OpremaLokacije.builder()
                        .oprema(oprema)
                        .lokacija(lokacija)
                        .build())
                .toList();

        if (lokacije.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Potrebna je barem jedna lokacija"));
        }

        oprema.setLokacije(new ArrayList<>(lokacije));

        Oprema spremljenaOprema = opremaRepo.save(oprema);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(spremljenaOprema));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isIzvodac = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_IZVODAC"));
 
        if (!isIzvodac) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Samo izvođači mogu brisati opremu"));
        }
 
        Oprema oprema = opremaRepo.findByIdOpreme(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oprema nije pronađena"));
 
        if (!oprema.getVlasnikOpreme().getUsernameIzvodac().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nemate ovlasti za brisanje ove opreme"));
        }
 
        opremaRepo.delete(oprema);
        return ResponseEntity.noContent().build();
    }

    private EquipmentResponse toResponse(Oprema oprema) {
        return new EquipmentResponse(
                oprema.getIdOpreme(),
                oprema.getNazivOpreme(),
                oprema.getCijena(),
                oprema.getKategorija(),
                oprema.getSlika(),
                oprema.getVlasnikOpreme().getUsernameIzvodac(),
                oprema.getLokacije().stream()
                        .map(OpremaLokacije::getLokacija)
                        .toList()
        );
    }

    private String normalizeSlika(String slika) {
        if (slika == null || slika.isBlank()) {
            return null;
        }

        return slika.trim();
    }
}
