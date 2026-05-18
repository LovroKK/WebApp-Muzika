package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.CreateRecenzijaRequest;
import hr.beatsync.backend.dto.RecenzijaResponse;
import hr.beatsync.backend.service.RecenzijaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/recenzije")
public class RecenzijaController {

    private final RecenzijaService recenzijaService;

    public RecenzijaController(RecenzijaService recenzijaService) {
        this.recenzijaService = recenzijaService;
    }

    @PostMapping
    public ResponseEntity<RecenzijaResponse> create(@Valid @RequestBody CreateRecenzijaRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isBusiness(auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo business korisnici mogu ostavljati recenzije");
        }
        RecenzijaResponse created = recenzijaService.createReview(auth.getName(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/dj/{username}")
    public ResponseEntity<List<RecenzijaResponse>> listForDj(@PathVariable String username) {
        return ResponseEntity.ok(recenzijaService.listApprovedForIzvodac(username));
    }

    private boolean isBusiness(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));
    }
}
