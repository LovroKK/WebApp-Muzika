package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.CheckoutSessionResponse;
import hr.beatsync.backend.dto.CreateCheckoutSessionRequest;
import hr.beatsync.backend.dto.PlacanjeResponse;
import hr.beatsync.backend.service.PlacanjeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/placanja")
public class PlacanjeController {

    private final PlacanjeService placanjeService;

    public PlacanjeController(PlacanjeService placanjeService) {
        this.placanjeService = placanjeService;
    }

    @PostMapping("/checkout-session")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @Valid @RequestBody CreateCheckoutSessionRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isBusiness(auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo business korisnici mogu pokrenuti plaćanje");
        }
        return ResponseEntity.ok(placanjeService.createCheckoutSession(auth.getName(), req));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(HttpServletRequest request,
                                          @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        if (signature == null || signature.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Stripe-Signature header");
        }
        String payload;
        try {
            payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot read body");
        }

        try {
            placanjeService.handleWebhookEvent(payload, signature);
            return ResponseEntity.ok("ok");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    @GetMapping("/by-session/{sessionId}")
    public ResponseEntity<PlacanjeResponse> getBySession(@PathVariable String sessionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isBusiness(auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo business korisnici");
        }
        return placanjeService.findBySession(sessionId, auth.getName())
                .map(p -> ResponseEntity.ok(PlacanjeResponse.from(p)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plaćanje ne postoji"));
    }

    @GetMapping("/by-rezervacija/{id}")
    public ResponseEntity<PlacanjeResponse> getByRezervacija(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isBusiness(auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo business korisnici");
        }
        return placanjeService.findLatestForRezervacija(id, auth.getName())
                .map(p -> ResponseEntity.ok(PlacanjeResponse.from(p)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plaćanje ne postoji"));
    }

    private boolean isBusiness(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));
    }
}
