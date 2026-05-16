package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.CreateEquipmentRequest;
import hr.beatsync.backend.dto.EquipmentResponse;
import hr.beatsync.backend.enums.KategorijaOpreme;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.model.Oprema;
import hr.beatsync.backend.model.OpremaLokacije;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import hr.beatsync.backend.repository.OpremaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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
    public ResponseEntity<?> create(
            @RequestParam("nazivOpreme") String nazivOpreme,
            @RequestParam("cijena") java.math.BigDecimal cijena,
            @RequestParam("kategorija") String kategorija,
            @RequestParam(value = "slika", required = false) MultipartFile slika,
            @RequestParam("lokacije") List<String> lokacije) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isIzvodac = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_IZVODAC"));

        if (!isIzvodac) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Samo izvođači mogu objaviti opremu"));
        }

        IzvodacKorisnik vlasnik = izvodacRepo.findById(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Izvođač nije pronađen"));

        byte[] slikaBytes = null;
        if (slika != null && !slika.isEmpty()) {
            validateImageFile(slika);
            try {
                slikaBytes = slika.getBytes();
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Greška pri čitanju datoteke");
            }
        }

        Oprema oprema = Oprema.builder()
                .nazivOpreme(nazivOpreme.trim())
                .cijena(cijena)
                .kategorija(KategorijaOpreme.fromValue(kategorija))
                .slika(slikaBytes)
                .vlasnikOpreme(vlasnik)
                .build();

        List<OpremaLokacije> lokacijeList = lokacije.stream()
                .map(String::trim)
                .filter(lokacija -> !lokacija.isBlank())
                .map(lokacija -> OpremaLokacije.builder()
                        .oprema(oprema)
                        .lokacija(lokacija)
                        .build())
                .toList();

        if (lokacijeList.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Potrebna je barem jedna lokacija"));
        }

        oprema.setLokacije(new ArrayList<>(lokacijeList));

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
        String slikaUrl = oprema.getSlika() != null ? "/api/equipment/" + oprema.getIdOpreme() + "/slika" : null;
        return new EquipmentResponse(
                oprema.getIdOpreme(),
                oprema.getNazivOpreme(),
                oprema.getCijena(),
                oprema.getKategorija(),
                slikaUrl,
                oprema.getVlasnikOpreme().getUsernameIzvodac(),
                oprema.getLokacije().stream()
                        .map(OpremaLokacije::getLokacija)
                        .toList()
        );
    }

    @GetMapping("/{id}/slika")
    public ResponseEntity<byte[]> getSlika(@PathVariable Integer id) {
        Oprema oprema = opremaRepo.findByIdOpreme(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oprema nije pronađena"));

        if (oprema.getSlika() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(oprema.getSlika());
    }

    @PostMapping("/{id}/slika")
    public ResponseEntity<?> uploadSlika(@PathVariable Integer id, @RequestParam("slika") MultipartFile slika) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Oprema oprema = opremaRepo.findByIdOpreme(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oprema nije pronađena"));

        if (!oprema.getVlasnikOpreme().getUsernameIzvodac().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nemate ovlasti za dodavanje slike ovoj opremi"));
        }

        validateImageFile(slika);

        try {
            oprema.setSlika(slika.getBytes());
            opremaRepo.save(oprema);
            return ResponseEntity.ok(Map.of("poruka", "Slika uspješno pohranjena"));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Greška pri čitanju datoteke");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datoteka je prazna");
        }
        String ct = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        System.out.println("DEBUG: contentType=" + ct + ", filename=" + originalFilename + ", size=" + file.getSize());

        if (ct == null || (!ct.startsWith("image/") && !isImageByExtension(originalFilename))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dopuštene su samo slike (content-type: " + ct + ")");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maksimalna veličina slike je 5MB");
        }
    }

    private boolean isImageByExtension(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
               lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp");
    }
}
