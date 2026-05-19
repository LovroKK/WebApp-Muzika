package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.KreirajNajamOpremeRequest;
import hr.beatsync.backend.dto.NajamOpremeResponse;
import hr.beatsync.backend.enums.StatusNajmaOpreme;
import hr.beatsync.backend.enums.TipKorisnika;
import hr.beatsync.backend.model.NajamOpreme;
import hr.beatsync.backend.model.Oprema;
import hr.beatsync.backend.model.PorukaOpreme;
import hr.beatsync.backend.repository.NajamOpremeRepository;
import hr.beatsync.backend.repository.OpremaRepository;
import hr.beatsync.backend.repository.PorukaOpremeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/najam-opreme")
public class NajamOpremeController {

    private final NajamOpremeRepository najamRepo;
    private final OpremaRepository opremaRepo;
    private final PorukaOpremeRepository porukaOpremeRepo;

    public NajamOpremeController(NajamOpremeRepository najamRepo,
                                 OpremaRepository opremaRepo,
                                 PorukaOpremeRepository porukaOpremeRepo) {
        this.najamRepo = najamRepo;
        this.opremaRepo = opremaRepo;
        this.porukaOpremeRepo = porukaOpremeRepo;
    }

    @PostMapping
    public ResponseEntity<?> kreirajZahtjev(@RequestBody KreirajNajamOpremeRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        if (req.getOpremaId() == null || req.getPeriodOd() == null || req.getPeriodDo() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nedostaju obavezni podaci"));
        }
        if (req.getPeriodOd().isAfter(req.getPeriodDo())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Period od mora biti prije period do"));
        }

        Oprema oprema = opremaRepo.findByIdOpreme(req.getOpremaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oprema ne postoji"));

        String vlasnikUsername = oprema.getVlasnikOpreme().getUsernameIzvodac();
        if (vlasnikUsername.equals(username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ne možete iznajmiti vlastitu opremu"));
        }

        NajamOpreme najam = NajamOpreme.builder()
                .oprema(oprema)
                .vlasnikUsername(vlasnikUsername)
                .najmoprimacUsername(username)
                .najmoprimacTip(isBusiness ? TipKorisnika.BUSINESS : TipKorisnika.IZVODAC)
                .periodOd(req.getPeriodOd())
                .periodDo(req.getPeriodDo())
                .napomena(req.getNapomena())
                .build();

        najamRepo.save(najam);

        // Kreiraj SYSTEM_NOTIFICATION za vlasnika opreme
        String opis = "Zahtjev za najam opreme \"" + oprema.getNazivOpreme() + "\" od " + req.getPeriodOd() + " do " + req.getPeriodDo() + ".";
        if (req.getNapomena() != null && !req.getNapomena().isBlank()) {
            opis += " Napomena: " + req.getNapomena();
        }
        PorukaOpreme notif = PorukaOpreme.builder()
                .najam(najam)
                .sadrzajPoruke(opis)
                .posiljateljeUsername(username)
                .messageType("SYSTEM_NOTIFICATION")
                .readStatus(false)
                .timestampPoruke(LocalDateTime.now())
                .build();
        porukaOpremeRepo.save(notif);

        return ResponseEntity.status(HttpStatus.CREATED).body(NajamOpremeResponse.from(najam));
    }

    @GetMapping
    public ResponseEntity<List<NajamOpremeResponse>> list() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isBusiness = isBusiness(auth);

        // Izvodac je vlasnik opreme → vidi primljene zahtjeve, i kao najmoprimac → vidi vlastite zahtjeve
        List<NajamOpreme> lista;
        if (isBusiness) {
            lista = najamRepo.findByNajmoprimacUsernameOrderByTimestampZahtjevaDesc(username);
        } else {
            // Izvodac može biti i vlasnik i najmoprimac — spoji oba
            List<NajamOpreme> kaoVlasnik = najamRepo.findByVlasnikUsernameOrderByTimestampZahtjevaDesc(username);
            List<NajamOpreme> kaoNajmoprimac = najamRepo.findByNajmoprimacUsernameOrderByTimestampZahtjevaDesc(username);
            lista = new java.util.ArrayList<>(kaoVlasnik);
            kaoNajmoprimac.stream()
                    .filter(n -> lista.stream().noneMatch(m -> m.getIdNajma().equals(n.getIdNajma())))
                    .forEach(lista::add);
            lista.sort((a, b) -> b.getTimestampZahtjeva().compareTo(a.getTimestampZahtjeva()));
        }

        return ResponseEntity.ok(lista.stream().map(NajamOpremeResponse::from).toList());
    }

    @PutMapping("/{id}/prihvati")
    public ResponseEntity<?> prihvati(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        NajamOpreme najam = getAndCheckVlasnik(id, username);

        if (najam.getStatus() != StatusNajmaOpreme.REQUESTED) {
            return ResponseEntity.badRequest().body(Map.of("message", "Zahtjev nije u statusu REQUESTED"));
        }

        najam.setStatus(StatusNajmaOpreme.ACCEPTED);
        najamRepo.save(najam);

        PorukaOpreme notif = PorukaOpreme.builder()
                .najam(najam)
                .sadrzajPoruke("Zahtjev za najam opreme \"" + najam.getOprema().getNazivOpreme() + "\" je prihvaćen.")
                .posiljateljeUsername(username)
                .messageType("SYSTEM_NOTIFICATION")
                .readStatus(false)
                .timestampPoruke(LocalDateTime.now())
                .build();
        porukaOpremeRepo.save(notif);

        return ResponseEntity.ok(NajamOpremeResponse.from(najam));
    }

    @PutMapping("/{id}/odbij")
    public ResponseEntity<?> odbij(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        NajamOpreme najam = getAndCheckVlasnik(id, username);

        if (najam.getStatus() != StatusNajmaOpreme.REQUESTED) {
            return ResponseEntity.badRequest().body(Map.of("message", "Zahtjev nije u statusu REQUESTED"));
        }

        najam.setStatus(StatusNajmaOpreme.REJECTED);
        najamRepo.save(najam);

        PorukaOpreme notif = PorukaOpreme.builder()
                .najam(najam)
                .sadrzajPoruke("Zahtjev za najam opreme \"" + najam.getOprema().getNazivOpreme() + "\" je odbijen.")
                .posiljateljeUsername(username)
                .messageType("SYSTEM_NOTIFICATION")
                .readStatus(false)
                .timestampPoruke(LocalDateTime.now())
                .build();
        porukaOpremeRepo.save(notif);

        return ResponseEntity.ok(NajamOpremeResponse.from(najam));
    }

    @PutMapping("/{id}/zavrsi")
    public ResponseEntity<?> zavrsi(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        NajamOpreme najam = getAndCheckVlasnik(id, username);

        if (najam.getStatus() != StatusNajmaOpreme.ACCEPTED && najam.getStatus() != StatusNajmaOpreme.IN_PROGRESS) {
            return ResponseEntity.badRequest().body(Map.of("message", "Završetak nije moguć u statusu: " + najam.getStatus()));
        }

        najam.setStatus(StatusNajmaOpreme.COMPLETED);
        najamRepo.save(najam);

        return ResponseEntity.ok(NajamOpremeResponse.from(najam));
    }

    private boolean isBusiness(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));
    }

    private NajamOpreme getAndCheckVlasnik(Integer id, String username) {
        NajamOpreme najam = najamRepo.findByIdNajma(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zahtjev za najam ne postoji"));
        if (!najam.getVlasnikUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo vlasnik opreme može mijenjati status zahtjeva");
        }
        return najam;
    }
}
