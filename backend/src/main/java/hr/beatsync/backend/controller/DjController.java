package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.DjSearchResponse;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/djs")
public class DjController {

    private final IzvodacKorisnikRepository izvodacRepo;

    public DjController(IzvodacKorisnikRepository izvodacRepo) {
        this.izvodacRepo = izvodacRepo;
    }

    @GetMapping
    public ResponseEntity<List<DjSearchResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minCijena,
            @RequestParam(required = false) BigDecimal maxCijena) {

        List<DjSearchResponse> rezultati = izvodacRepo
                .search(search, minCijena, maxCijena)
                .stream()
                .map(DjSearchResponse::from)
                .toList();

        return ResponseEntity.ok(rezultati);
    }
}
