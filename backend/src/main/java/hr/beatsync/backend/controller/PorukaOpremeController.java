package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.PorukaOpremeResponse;
import hr.beatsync.backend.dto.SendPorukaOpremeRequest;
import hr.beatsync.backend.model.NajamOpreme;
import hr.beatsync.backend.model.PorukaOpreme;
import hr.beatsync.backend.repository.NajamOpremeRepository;
import hr.beatsync.backend.repository.PorukaOpremeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/poruke-opreme")
public class PorukaOpremeController {

    private final PorukaOpremeRepository porukaOpremeRepo;
    private final NajamOpremeRepository najamRepo;

    public PorukaOpremeController(PorukaOpremeRepository porukaOpremeRepo,
                                  NajamOpremeRepository najamRepo) {
        this.porukaOpremeRepo = porukaOpremeRepo;
        this.najamRepo = najamRepo;
    }

    @GetMapping
    public ResponseEntity<List<PorukaOpremeResponse>> getPoruke(@RequestParam Integer najam) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        NajamOpreme n = getNajamAndCheckAccess(najam, username);

        List<PorukaOpreme> poruke = porukaOpremeRepo.findByNajam_IdNajmaOrderByTimestampPorukeAsc(najam);

        // Označi nepročitane kao pročitane za calling stranu
        List<PorukaOpreme> neprocitane = porukaOpremeRepo.findUnreadByNajamForUser(najam, username);
        if (!neprocitane.isEmpty()) {
            neprocitane.forEach(p -> p.setReadStatus(true));
            porukaOpremeRepo.saveAll(neprocitane);
        }

        return ResponseEntity.ok(poruke.stream().map(PorukaOpremeResponse::from).toList());
    }

    @PostMapping
    public ResponseEntity<?> posaljiPoruku(@RequestBody SendPorukaOpremeRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        if (req.getSadrzajPoruke() == null || req.getSadrzajPoruke().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Poruka ne može biti prazna"));
        }
        if (req.getNajamId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nedostaje najamId"));
        }

        NajamOpreme najam = getNajamAndCheckAccess(req.getNajamId(), username);

        // Najmoprimac može pisati tek kad vlasnik otvori razgovor
        boolean isNajmoprimac = najam.getNajmoprimacUsername().equals(username);
        if (isNajmoprimac && !porukaOpremeRepo.existsByNajam_IdNajmaAndMessageType(req.getNajamId(), "CHAT")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Možete odgovoriti tek kada vlasnik opreme otvori razgovor"));
        }

        PorukaOpreme poruka = PorukaOpreme.builder()
                .najam(najam)
                .sadrzajPoruke(req.getSadrzajPoruke())
                .posiljateljeUsername(username)
                .messageType("CHAT")
                .readStatus(false)
                .timestampPoruke(LocalDateTime.now())
                .build();

        porukaOpremeRepo.save(poruka);
        return ResponseEntity.ok(PorukaOpremeResponse.from(poruka));
    }

    // Vlasnik opreme inicira razgovor
    @PostMapping("/otvori-chat/{najamId}")
    public ResponseEntity<?> otvorChat(@PathVariable Integer najamId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        NajamOpreme najam = najamRepo.findByIdNajma(najamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zahtjev za najam ne postoji"));

        if (!najam.getVlasnikUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Samo vlasnik opreme može inicirati razgovor"));
        }

        if (porukaOpremeRepo.existsByNajam_IdNajmaAndMessageType(najamId, "CHAT")) {
            return ResponseEntity.ok(Map.of("poruka", "Razgovor već postoji"));
        }

        PorukaOpreme prvaPorukaChat = PorukaOpreme.builder()
                .najam(najam)
                .sadrzajPoruke("Pozdrav! Zaprimio sam vaš zahtjev za najam opreme \""
                        + najam.getOprema().getNazivOpreme()
                        + "\". Možemo se dogovoriti o detaljima.")
                .posiljateljeUsername(username)
                .messageType("CHAT")
                .readStatus(false)
                .timestampPoruke(LocalDateTime.now())
                .build();

        porukaOpremeRepo.save(prvaPorukaChat);
        return ResponseEntity.ok(Map.of("poruka", "Razgovor uspješno otvoren"));
    }

    @GetMapping("/inbox")
    public ResponseEntity<?> inbox() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        List<Map<String, Object>> rezultat = new ArrayList<>();

        if (!isBusiness) {
            // Izvodac kao vlasnik — zahtjevi (SYSTEM_NOTIFICATION bez CHAT)
            List<PorukaOpreme> zahtjevi = porukaOpremeRepo.findZahtjeviForVlasnik(username);
            for (PorukaOpreme p : zahtjevi) {
                Map<String, Object> item = buildInboxItem(p, true);
                item.put("tip", "ZAHTJEV_NAJMA");
                rezultat.add(item);
            }

            // Izvodac kao vlasnik — razgovori (CHAT poruke)
            List<PorukaOpreme> razgovoriVlasnik = porukaOpremeRepo.findRazgovoriForVlasnik(username);
            for (PorukaOpreme p : razgovoriVlasnik) {
                Map<String, Object> item = buildInboxItem(p, true);
                item.put("tip", "RAZGOVOR_NAJMA");
                rezultat.add(item);
            }

            // Izvodac kao najmoprimac — razgovori
            List<PorukaOpreme> razgovoriNajmoprimac = porukaOpremeRepo.findRazgovoriForNajmoprimac(username);
            for (PorukaOpreme p : razgovoriNajmoprimac) {
                if (rezultat.stream().noneMatch(i -> i.get("najamId").equals(p.getNajam().getIdNajma()))) {
                    Map<String, Object> item = buildInboxItem(p, false);
                    item.put("tip", "RAZGOVOR_NAJMA");
                    rezultat.add(item);
                }
            }
        } else {
            // Business kao najmoprimac — razgovori
            List<PorukaOpreme> razgovori = porukaOpremeRepo.findRazgovoriForNajmoprimac(username);
            for (PorukaOpreme p : razgovori) {
                Map<String, Object> item = buildInboxItem(p, false);
                item.put("tip", "RAZGOVOR_NAJMA");
                rezultat.add(item);
            }
        }

        rezultat.sort((a, b) -> {
            LocalDateTime ta = (LocalDateTime) a.get("timestamp");
            LocalDateTime tb = (LocalDateTime) b.get("timestamp");
            return tb.compareTo(ta);
        });

        return ResponseEntity.ok(rezultat);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        long count = porukaOpremeRepo.countUnreadForUser(username);
        return ResponseEntity.ok(Map.of("count", count));
    }

    private boolean isBusiness(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));
    }

    private NajamOpreme getNajamAndCheckAccess(Integer najamId, String username) {
        NajamOpreme najam = najamRepo.findByIdNajma(najamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zahtjev za najam ne postoji"));
        boolean hasAccess = najam.getVlasnikUsername().equals(username)
                || najam.getNajmoprimacUsername().equals(username);
        if (!hasAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pristup ovom zahtjevu");
        }
        return najam;
    }

    private Map<String, Object> buildInboxItem(PorukaOpreme p, boolean isVlasnik) {
        Map<String, Object> item = new LinkedHashMap<>();
        NajamOpreme najam = p.getNajam();
        item.put("najamId", najam.getIdNajma());
        item.put("nazivOpreme", najam.getOprema().getNazivOpreme());
        item.put("kategorijaOpreme", najam.getOprema().getKategorija().name());
        item.put("vlasnikUsername", najam.getVlasnikUsername());
        item.put("najmoprimacUsername", najam.getNajmoprimacUsername());
        item.put("najmoprimacTip", najam.getNajmoprimacTip().name());
        item.put("statusNajma", najam.getStatus().name());
        item.put("periodOd", najam.getPeriodOd());
        item.put("periodDo", najam.getPeriodDo());
        item.put("lastMessage", p.getSadrzajPoruke());
        item.put("timestamp", p.getTimestampPoruke());
        item.put("readStatus", p.getReadStatus());
        item.put("messageType", p.getMessageType());
        return item;
    }
}
