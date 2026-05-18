package hr.beatsync.backend.controller;

import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.model.BusinessKorisnik;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.model.Poruka;
import hr.beatsync.backend.model.Rezervacija;
import hr.beatsync.backend.repository.BusinessKorisnikRepository;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import hr.beatsync.backend.repository.PorukaRepository;
import hr.beatsync.backend.repository.RezervacijaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final IzvodacKorisnikRepository izvodacRepo;
    private final BusinessKorisnikRepository businessRepo;
    private final PorukaRepository porukaRepo;
    private final RezervacijaRepository rezervacijaRepo;

    public AdminController(IzvodacKorisnikRepository izvodacRepo,
                           BusinessKorisnikRepository businessRepo,
                           PorukaRepository porukaRepo,
                           RezervacijaRepository rezervacijaRepo) {
        this.izvodacRepo = izvodacRepo;
        this.businessRepo = businessRepo;
        this.porukaRepo = porukaRepo;
        this.rezervacijaRepo = rezervacijaRepo;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalIzvodaci = izvodacRepo.count();
        long totalBusinessi = businessRepo.count();
        long totalRezervacije = rezervacijaRepo.count();
        long totalPoruke = porukaRepo.count();

        long aktivneRezervacije = rezervacijaRepo.findAll().stream()
                .filter(r -> r.getStatusRezervacije() == StatusRezervacije.ACCEPTED
                        || r.getStatusRezervacije() == StatusRezervacije.IN_PROGRESS)
                .count();

        Map<String, Long> rezervacijePoStatusu = new HashMap<>();
        for (StatusRezervacije status : StatusRezervacije.values()) {
            long count = rezervacijaRepo.findAll().stream()
                    .filter(r -> r.getStatusRezervacije() == status)
                    .count();
            rezervacijePoStatusu.put(status.name(), count);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIzvodaci", totalIzvodaci);
        stats.put("totalBusinessi", totalBusinessi);
        stats.put("totalRezervacije", totalRezervacije);
        stats.put("aktivneRezervacije", aktivneRezervacije);
        stats.put("totalPoruke", totalPoruke);
        stats.put("rezervacijePoStatusu", rezervacijePoStatusu);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "SVE") String tip) {

        List<Map<String, Object>> users = new java.util.ArrayList<>();

        if (tip.equals("SVE") || tip.equals("IZVODAC")) {
            for (IzvodacKorisnik i : izvodacRepo.findAll()) {
                Map<String, Object> u = new HashMap<>();
                u.put("username", i.getUsernameIzvodac());
                u.put("tip", "IZVODAC");
                u.put("email", i.getEmail());
                u.put("ime", i.getIme() + " " + i.getPrezime());
                users.add(u);
            }
        }

        if (tip.equals("SVE") || tip.equals("BUSINESS")) {
            for (BusinessKorisnik b : businessRepo.findAll()) {
                Map<String, Object> u = new HashMap<>();
                u.put("username", b.getUsernameBusiness());
                u.put("tip", "BUSINESS");
                u.put("email", b.getEmail());
                u.put("ime", b.getNazivKluba());
                users.add(u);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("total", users.size());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/izvodac/{username}")
    @Transactional
    public ResponseEntity<Void> deleteIzvodac(@PathVariable String username) {
        if (!izvodacRepo.existsByUsernameIzvodac(username)) {
            return ResponseEntity.notFound().build();
        }
        // Nullaj resident_dj referencu kod business korisnika koji ima ovog DJ-a
        businessRepo.findByResidentDj_UsernameIzvodac(username).forEach(b -> {
            b.setResidentDj(null);
            businessRepo.save(b);
        });
        // Obriši poruke — nema cascade na Poruka entitetu
        porukaRepo.deleteByIzvodacPoruka_UsernameIzvodac(username);
        // JPA cascade (CascadeType.ALL) briše Oprema, OpremaLokacije, Rezervacija, Recenzija
        izvodacRepo.deleteById(username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/business/{username}")
    @Transactional
    public ResponseEntity<Void> deleteBusiness(@PathVariable String username) {
        if (!businessRepo.existsByUsernameBusiness(username)) {
            return ResponseEntity.notFound().build();
        }
        // Obriši poruke — nema cascade na Poruka entitetu
        porukaRepo.deleteByBusinessPoruka_UsernameBusiness(username);
        // JPA cascade (CascadeType.ALL) briše JobOffer, Rezervacija, Recenzija, SlikeProstora
        businessRepo.deleteById(username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/messages")
    public ResponseEntity<Map<String, Object>> getMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Poruka> pageResult = porukaRepo.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestampPoruke")));

        List<Map<String, Object>> messages = pageResult.getContent().stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getIdPoruke());
            m.put("sadrzaj", p.getSadrzajPoruke());
            m.put("timestamp", p.getTimestampPoruke());
            m.put("tip", p.getMessageType());
            m.put("posiljatelj", p.getPosiljatelj().name());
            m.put("usernameIzvodac", p.getIzvodacPoruka().getUsernameIzvodac());
            m.put("usernameBusiness", p.getBusinessPoruka().getUsernameBusiness());
            m.put("idRezervacije", p.getIdRezervacije());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);
        response.put("totalPages", pageResult.getTotalPages());
        response.put("totalElements", pageResult.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Integer id) {
        if (!porukaRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        porukaRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> getBookings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Rezervacija> sve = rezervacijaRepo.findAll();

        List<Map<String, Object>> bookings = sve.stream()
                .filter(r -> status == null || status.isBlank()
                        || r.getStatusRezervacije().name().equals(status))
                .sorted((a, b) -> b.getIdRezervacije().compareTo(a.getIdRezervacije()))
                .skip((long) page * size)
                .limit(size)
                .map(r -> {
                    Map<String, Object> b = new HashMap<>();
                    b.put("id", r.getIdRezervacije());
                    b.put("status", r.getStatusRezervacije().name());
                    b.put("periodOd", r.getPeriodOd());
                    b.put("periodDo", r.getPeriodDo());
                    b.put("usernameIzvodac", r.getIzvodacRezervacija().getUsernameIzvodac());
                    b.put("usernameBusiness", r.getBusinessRezervacija().getUsernameBusiness());
                    b.put("potvrdaIzvodac", r.getPotvrdaIzvodac());
                    b.put("potvrdaBusiness", r.getPotvrdaBusiness());
                    return b;
                })
                .collect(Collectors.toList());

        long totalFiltered = sve.stream()
                .filter(r -> status == null || status.isBlank()
                        || r.getStatusRezervacije().name().equals(status))
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("bookings", bookings);
        response.put("total", totalFiltered);
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }
}
