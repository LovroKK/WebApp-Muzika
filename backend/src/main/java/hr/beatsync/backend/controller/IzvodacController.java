package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.IzvodacProfilResponse;
import hr.beatsync.backend.dto.UpdateIzvodacRequest;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import hr.beatsync.backend.repository.RecenzijaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final RecenzijaRepository recenzijaRepo;

    public IzvodacController(IzvodacKorisnikRepository izvodacRepo,
                             RecenzijaRepository recenzijaRepo) {
        this.izvodacRepo = izvodacRepo;
        this.recenzijaRepo = recenzijaRepo;
    }

    @GetMapping("/profil")
    public ResponseEntity<IzvodacProfilResponse> getProfil() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        IzvodacKorisnik izvodac = izvodacRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji"));
        IzvodacProfilResponse dto = IzvodacProfilResponse.from(izvodac);
        populateAggregate(dto, username);
        return ResponseEntity.ok(dto);
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
        IzvodacProfilResponse dto = IzvodacProfilResponse.from(izvodac);
        populateAggregate(dto, username);
        return ResponseEntity.ok(dto);
    }

    private void populateAggregate(IzvodacProfilResponse dto, String username) {
        var agg = recenzijaRepo.findAggregateForIzvodac(username);
        if (agg == null || agg.getCnt() == null || agg.getCnt() == 0) {
            dto.setAvgOcjena(null);
            dto.setBrojRecenzija(0L);
        } else {
            dto.setAvgOcjena(BigDecimal.valueOf(agg.getAvg()).setScale(1, RoundingMode.HALF_UP));
            dto.setBrojRecenzija(agg.getCnt());
        }
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
