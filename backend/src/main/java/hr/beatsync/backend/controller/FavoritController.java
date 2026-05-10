package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.FavoritResponse;
import hr.beatsync.backend.model.Favorit;
import hr.beatsync.backend.repository.FavoritRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/favoriti")
public class FavoritController {

    private final FavoritRepository favoritRepo;

    public FavoritController(FavoritRepository favoritRepo) {
        this.favoritRepo = favoritRepo;
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Boolean>> toggle(
            @RequestParam String tipFavorita,
            @RequestParam String favoritUsername) {

        String korisnik = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Favorit> postojeci = favoritRepo.findByKorisnikUsernameAndTipFavoritaAndFavoritUsername(
                korisnik, tipFavorita, favoritUsername);

        if (postojeci.isPresent()) {
            favoritRepo.delete(postojeci.get());
            return ResponseEntity.ok(Map.of("favorit", false));
        } else {
            favoritRepo.save(new Favorit(korisnik, tipFavorita, favoritUsername));
            return ResponseEntity.ok(Map.of("favorit", true));
        }
    }

    @GetMapping
    public ResponseEntity<List<FavoritResponse>> getFavoriti() {
        String korisnik = SecurityContextHolder.getContext().getAuthentication().getName();
        List<FavoritResponse> lista = favoritRepo.findByKorisnikUsername(korisnik)
                .stream()
                .map(f -> new FavoritResponse(f.getTipFavorita(), f.getFavoritUsername()))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> check(
            @RequestParam String tipFavorita,
            @RequestParam String favoritUsername) {

        String korisnik = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isFavorit = favoritRepo.existsByKorisnikUsernameAndTipFavoritaAndFavoritUsername(
                korisnik, tipFavorita, favoritUsername);
        return ResponseEntity.ok(Map.of("favorit", isFavorit));
    }
}
