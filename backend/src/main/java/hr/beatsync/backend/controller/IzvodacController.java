package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.IzvodacProfilResponse;
import hr.beatsync.backend.dto.UpdateIzvodacRequest;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
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

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        IzvodacKorisnik izvodac = izvodacRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));

        validateImageFile(file);

        try {
            izvodac.setLogoAvatar(file.getBytes());
            izvodacRepo.save(izvodac);
            return ResponseEntity.ok(Map.of("poruka", "Avatar uspješno pohranjen"));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Greška pri čitanju datoteke");
        }
    }

    @GetMapping("/avatar/{username}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String username) {
        IzvodacKorisnik izvodac = izvodacRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));

        if (izvodac.getLogoAvatar() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(izvodac.getLogoAvatar());
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
