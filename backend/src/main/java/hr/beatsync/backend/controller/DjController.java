package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.DjSearchResponse;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import hr.beatsync.backend.repository.RecenzijaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api/djs")
public class DjController {

    private final IzvodacKorisnikRepository izvodacRepo;
    private final RecenzijaRepository recenzijaRepo;

    public DjController(IzvodacKorisnikRepository izvodacRepo,
                        RecenzijaRepository recenzijaRepo) {
        this.izvodacRepo = izvodacRepo;
        this.recenzijaRepo = recenzijaRepo;
    }

    @GetMapping
    public ResponseEntity<List<DjSearchResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minCijena,
            @RequestParam(required = false) BigDecimal maxCijena) {

        List<DjSearchResponse> rezultati = izvodacRepo
                .search(search, minCijena, maxCijena)
                .stream()
                .map(i -> {
                    DjSearchResponse dto = DjSearchResponse.from(i);
                    var agg = recenzijaRepo.findAggregateForIzvodac(i.getUsernameIzvodac());
                    populateAggregate(dto, agg);
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(rezultati);
    }

    private void populateAggregate(DjSearchResponse dto, RecenzijaRepository.OcjenaAggregate agg) {
        if (agg == null || agg.getCnt() == null || agg.getCnt() == 0) {
            dto.setAvgOcjena(null);
            dto.setBrojRecenzija(0L);
        } else {
            dto.setAvgOcjena(BigDecimal.valueOf(agg.getAvg()).setScale(1, RoundingMode.HALF_UP));
            dto.setBrojRecenzija(agg.getCnt());
        }
    }
}
