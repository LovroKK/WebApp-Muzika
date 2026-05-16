package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.BusinessProfilResponse;
import hr.beatsync.backend.dto.SlikeProstoraResponse;
import hr.beatsync.backend.dto.UpdateBusinessRequest;
import hr.beatsync.backend.model.BusinessKorisnik;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.model.SlikeProstora;
import hr.beatsync.backend.repository.BusinessKorisnikRepository;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import hr.beatsync.backend.repository.SlikeProstoraRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessKorisnikRepository businessRepo;
    private final IzvodacKorisnikRepository izvodacRepo;
    private final SlikeProstoraRepository slikeProstoraRepo;

    public BusinessController(BusinessKorisnikRepository businessRepo,
                               IzvodacKorisnikRepository izvodacRepo,
                               SlikeProstoraRepository slikeProstoraRepo) {
        this.businessRepo = businessRepo;
        this.izvodacRepo = izvodacRepo;
        this.slikeProstoraRepo = slikeProstoraRepo;
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

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BusinessKorisnik business = businessRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));

        validateImageFile(file);

        try {
            business.setLogoAvatar(file.getBytes());
            businessRepo.save(business);
            return ResponseEntity.ok(Map.of("poruka", "Avatar uspješno pohranjen"));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Greška pri čitanju datoteke");
        }
    }

    @GetMapping("/avatar/{username}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String username) {
        BusinessKorisnik business = businessRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));

        if (business.getLogoAvatar() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(business.getLogoAvatar());
    }

    @PostMapping("/slike")
    public ResponseEntity<SlikeProstoraResponse> uploadSlika(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "opis", required = false) String opis) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BusinessKorisnik business = businessRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));

        validateImageFile(file);

        try {
            SlikeProstora slika = SlikeProstora.builder()
                    .businessSlike(business)
                    .slikaData(file.getBytes())
                    .opisSlike(opis)
                    .build();
            SlikeProstora saved = slikeProstoraRepo.save(slika);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SlikeProstoraResponse(
                            saved.getIdSlike(),
                            saved.getOpisSlike(),
                            "/api/business/slike/" + saved.getIdSlike()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Greška pri čitanju datoteke");
        }
    }

    @GetMapping("/slike/{id}")
    public ResponseEntity<byte[]> getSlika(@PathVariable Integer id) {
        SlikeProstora slika = slikeProstoraRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slika ne postoji"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(slika.getSlikaData());
    }

    @DeleteMapping("/slike/{id}")
    public ResponseEntity<?> deleteSlika(@PathVariable Integer id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SlikeProstora slika = slikeProstoraRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slika ne postoji"));

        if (!slika.getBusinessSlike().getUsernameBusiness().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nemate dozvolu za brisanje ove slike"));
        }

        slikeProstoraRepo.delete(slika);
        return ResponseEntity.ok(Map.of("poruka", "Slika obrisana"));
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
