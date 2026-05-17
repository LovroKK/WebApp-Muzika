package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.PorukaResponse;
import hr.beatsync.backend.dto.SendPorukaRequest;
import hr.beatsync.backend.enums.VrstaPosiljatelja;
import hr.beatsync.backend.model.*;
import hr.beatsync.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/poruke")
public class PorukaController {

    private final PorukaRepository porukaRepo;
    private final RezervacijaRepository rezervacijaRepo;
    private final IzvodacKorisnikRepository izvodacRepo;
    private final BusinessKorisnikRepository businessRepo;

    public PorukaController(PorukaRepository porukaRepo,
                            RezervacijaRepository rezervacijaRepo,
                            IzvodacKorisnikRepository izvodacRepo,
                            BusinessKorisnikRepository businessRepo) {
        this.porukaRepo = porukaRepo;
        this.rezervacijaRepo = rezervacijaRepo;
        this.izvodacRepo = izvodacRepo;
        this.businessRepo = businessRepo;
    }

    // Dohvati sve poruke za konkretnu rezervaciju (chat + system notif)
    @GetMapping
    public ResponseEntity<List<PorukaResponse>> getPoruke(@RequestParam Integer rezervacija) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        Rezervacija rez = rezervacijaRepo.findByIdRezervacije(rezervacija)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervacija ne postoji"));

        // Provjera pristupa
        if (isBusiness && !rez.getBusinessRezervacija().getUsernameBusiness().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!isBusiness && !rez.getIzvodacRezervacija().getUsernameIzvodac().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Poruka> poruke = porukaRepo.findByIdRezervacijeOrderByTimestampPorukeAsc(rezervacija);

        // Označi nepročitane poruke kao pročitane za calling stranu
        List<Poruka> neprocitane = isBusiness
                ? porukaRepo.findUnreadByRezervacijaForBusiness(rezervacija)
                : porukaRepo.findUnreadByRezervacijaForIzvodac(rezervacija);
        if (!neprocitane.isEmpty()) {
            neprocitane.forEach(p -> p.setReadStatus(true));
            porukaRepo.saveAll(neprocitane);
        }

        return ResponseEntity.ok(poruke.stream().map(PorukaResponse::from).toList());
    }

    // Pošalji CHAT poruku (IZVODAC može samo ako BUSINESS već poslao prvu)
    @PostMapping
    public ResponseEntity<?> posaljiPoruku(@RequestBody SendPorukaRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        if (req.getSadrzajPoruke() == null || req.getSadrzajPoruke().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Poruka ne može biti prazna"));
        }
        if (req.getIdRezervacije() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nedostaje idRezervacije"));
        }

        Rezervacija rez = rezervacijaRepo.findByIdRezervacije(req.getIdRezervacije())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervacija ne postoji"));

        // Provjera pristupa
        if (isBusiness && !rez.getBusinessRezervacija().getUsernameBusiness().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Nemate pristup ovoj rezervaciji"));
        }
        if (!isBusiness && !rez.getIzvodacRezervacija().getUsernameIzvodac().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Nemate pristup ovoj rezervaciji"));
        }

        // IZVODAC može pisati samo ako BUSINESS već inicirao razgovor
        if (!isBusiness && !porukaRepo.existsByIdRezervacijeAndMessageType(req.getIdRezervacije(), "CHAT")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Možete odgovoriti tek kada klub otvori razgovor"));
        }

        Poruka poruka = Poruka.builder()
                .sadrzajPoruke(req.getSadrzajPoruke())
                .posiljatelj(isBusiness ? VrstaPosiljatelja.BUSINESS : VrstaPosiljatelja.IZVODAC)
                .izvodacPoruka(rez.getIzvodacRezervacija())
                .businessPoruka(rez.getBusinessRezervacija())
                .idRezervacije(req.getIdRezervacije())
                .messageType("CHAT")
                .readStatus(false)
                .timestampPoruke(LocalDateTime.now())
                .build();

        porukaRepo.save(poruka);
        return ResponseEntity.ok(PorukaResponse.from(poruka));
    }

    // BUSINESS inicira razgovor — kreira prvu CHAT poruku i "prebacuje" zahtjev u razgovor
    @PostMapping("/otvori-chat/{rezervacijaId}")
    public ResponseEntity<?> otвориChat(@PathVariable Integer rezervacijaId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        if (!isBusiness) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Samo business korisnici mogu inicirati razgovor"));
        }

        Rezervacija rez = rezervacijaRepo.findByIdRezervacije(rezervacijaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervacija ne postoji"));

        if (!rez.getBusinessRezervacija().getUsernameBusiness().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nemate pristup ovoj rezervaciji"));
        }

        // Ne otvori dvaput razgovor
        if (porukaRepo.existsByIdRezervacijeAndMessageType(rezervacijaId, "CHAT")) {
            return ResponseEntity.ok(Map.of("poruka", "Razgovor već postoji"));
        }

        BusinessKorisnik business = rez.getBusinessRezervacija();
        IzvodacKorisnik izvodac = rez.getIzvodacRezervacija();

        Poruka prvaPorukaChat = Poruka.builder()
                .sadrzajPoruke("Pozdrav! Hvala na prijavi na ponudu \"" + rez.getJobOffer().getNazivPonude()
                        + "\". Možemo razgovoriti detalje suradnje.")
                .posiljatelj(VrstaPosiljatelja.BUSINESS)
                .izvodacPoruka(izvodac)
                .businessPoruka(business)
                .idRezervacije(rezervacijaId)
                .messageType("CHAT")
                .readStatus(false)
                .timestampPoruke(LocalDateTime.now())
                .build();

        porukaRepo.save(prvaPorukaChat);
        return ResponseEntity.ok(Map.of("poruka", "Razgovor uspješno otvoren"));
    }

    // Inbox — Zahtjevi tab za BUSINESS
    @GetMapping("/inbox")
    public ResponseEntity<?> inbox(@RequestParam(defaultValue = "RAZGOVORI") String type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        if (isBusiness) {
            if ("ZAHTJEVI".equals(type)) {
                List<Poruka> zahtjevi = porukaRepo.findZahtjeviForBusiness(username);
                return ResponseEntity.ok(zahtjevi.stream().map(p -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("idRezervacije", p.getIdRezervacije());
                    item.put("izvodacUsername", p.getIzvodacPoruka().getUsernameIzvodac());
                    item.put("izvodacIme", p.getIzvodacPoruka().getIme());
                    item.put("izvodacPrezime", p.getIzvodacPoruka().getPrezime());
                    item.put("nazivPonude", p.getIdRezervacije() != null
                            ? rezervacijaRepo.findByIdRezervacije(p.getIdRezervacije())
                                    .map(r -> r.getJobOffer() != null ? r.getJobOffer().getNazivPonude() : "")
                                    .orElse("")
                            : "");
                    item.put("tekst", p.getSadrzajPoruke());
                    item.put("timestamp", p.getTimestampPoruke());
                    item.put("readStatus", p.getReadStatus());
                    return item;
                }).toList());
            } else {
                List<Poruka> razgovori = porukaRepo.findRazgovoriForBusiness(username);
                return ResponseEntity.ok(buildRazgovoriResponse(razgovori, true));
            }
        } else {
            // IZVODAC vidi razgovore + odbijanja (SYSTEM_NOTIFICATION od BUSINESS bez CHAT poruka)
            List<Poruka> razgovori = porukaRepo.findRazgovoriForIzvodac(username);
            List<Poruka> odbijanja = porukaRepo.findOdbijanjaForIzvodac(username);

            // Spoji: razgovori imaju prednost (ako ista rezervacija ima i CHAT i odbijanje, CHAT pobijedi)
            java.util.Set<Integer> rezIdChatSet = razgovori.stream()
                    .map(Poruka::getIdRezervacije)
                    .collect(java.util.stream.Collectors.toSet());
            List<Poruka> odbijanjaFiltered = odbijanja.stream()
                    .filter(p -> !rezIdChatSet.contains(p.getIdRezervacije()))
                    .toList();

            List<Map<String, Object>> rezultat = new java.util.ArrayList<>(buildRazgovoriResponse(razgovori, false));
            rezultat.addAll(buildOdbijanjaResponse(odbijanjaFiltered));
            rezultat.sort((a, b) -> {
                java.time.LocalDateTime ta = (java.time.LocalDateTime) a.get("timestamp");
                java.time.LocalDateTime tb = (java.time.LocalDateTime) b.get("timestamp");
                return tb.compareTo(ta);
            });
            return ResponseEntity.ok(rezultat);
        }
    }

    // Ukupni broj nepročitanih za badge na chat ikoni
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        long count = isBusiness
                ? porukaRepo.countUnreadForBusiness(username)
                : porukaRepo.countUnreadForIzvodac(username);

        return ResponseEntity.ok(Map.of("count", count));
    }

    private boolean isBusiness(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));
    }

    private List<Map<String, Object>> buildOdbijanjaResponse(List<Poruka> poruke) {
        return poruke.stream().map(p -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idRezervacije", p.getIdRezervacije());
            item.put("izvodacUsername", p.getIzvodacPoruka().getUsernameIzvodac());
            item.put("izvodacIme", p.getIzvodacPoruka().getIme());
            item.put("izvodacPrezime", p.getIzvodacPoruka().getPrezime());
            item.put("businessUsername", p.getBusinessPoruka().getUsernameBusiness());
            item.put("nazivKluba", p.getBusinessPoruka().getNazivKluba());
            item.put("lastMessage", p.getSadrzajPoruke());
            item.put("timestamp", p.getTimestampPoruke());
            item.put("readStatus", p.getReadStatus());
            item.put("messageType", "SYSTEM_NOTIFICATION");
            item.put("statusRezervacije", "CANCELLED");
            if (p.getIdRezervacije() != null) {
                rezervacijaRepo.findByIdRezervacije(p.getIdRezervacije()).ifPresent(r -> {
                    if (r.getJobOffer() != null) {
                        item.put("nazivPonude", r.getJobOffer().getNazivPonude());
                        item.put("datumPonude", r.getJobOffer().getDatum());
                    }
                });
            }
            return item;
        }).toList();
    }

    private List<Map<String, Object>> buildRazgovoriResponse(List<Poruka> poruke, boolean isBusiness) {
        return poruke.stream().map(p -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idRezervacije", p.getIdRezervacije());
            item.put("izvodacUsername", p.getIzvodacPoruka().getUsernameIzvodac());
            item.put("izvodacIme", p.getIzvodacPoruka().getIme());
            item.put("izvodacPrezime", p.getIzvodacPoruka().getPrezime());
            item.put("businessUsername", p.getBusinessPoruka().getUsernameBusiness());
            item.put("nazivKluba", p.getBusinessPoruka().getNazivKluba());
            item.put("lastMessage", p.getSadrzajPoruke());
            item.put("timestamp", p.getTimestampPoruke());
            item.put("readStatus", p.getReadStatus());
            if (p.getIdRezervacije() != null) {
                rezervacijaRepo.findByIdRezervacije(p.getIdRezervacije()).ifPresent(r -> {
                    item.put("statusRezervacije", r.getStatusRezervacije().name());
                    item.put("potvrdaIzvodac", r.getPotvrdaIzvodac());
                    item.put("potvrdaBusiness", r.getPotvrdaBusiness());
                    if (r.getJobOffer() != null) {
                        item.put("nazivPonude", r.getJobOffer().getNazivPonude());
                        item.put("datumPonude", r.getJobOffer().getDatum());
                    }
                });
            }
            return item;
        }).toList();
    }
}
