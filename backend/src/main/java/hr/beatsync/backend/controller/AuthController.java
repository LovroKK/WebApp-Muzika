package hr.beatsync.backend.controller;

import hr.beatsync.backend.dto.*;
import hr.beatsync.backend.model.BusinessKorisnik;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.repository.BusinessKorisnikRepository;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import hr.beatsync.backend.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


// ----------------------------------------------------------------------------//
//                                ALL ENDPOINTS
// ----------------------------------------------------------------------------//

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IzvodacKorisnikRepository izvodacRepo;
    private final BusinessKorisnikRepository businessRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(IzvodacKorisnikRepository izvodacRepo,
                          BusinessKorisnikRepository businessRepo,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.izvodacRepo = izvodacRepo;
        this.businessRepo = businessRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register/izvodac")
    public ResponseEntity<AuthResponse> registerIzvodac(
            @Valid @RequestBody RegisterIzvodacRequest request) {

        if (izvodacRepo.existsByUsernameIzvodac(request.getUsernameIzvodac())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username već postoji");
        }

        IzvodacKorisnik izvodac = new IzvodacKorisnik();
        izvodac.setUsernameIzvodac(request.getUsernameIzvodac());
        izvodac.setIme(request.getIme());
        izvodac.setPrezime(request.getPrezime());
        izvodac.setEmail(request.getEmail());
        izvodac.setBrojTelefona(request.getBrojTelefona());
        izvodac.setLozinka(passwordEncoder.encode(request.getLozinka()));
        izvodac.setLinkMixtape(request.getLinkMixtape());
        izvodac.setCijenaPoSatu(request.getCijenaPoSatu());
        izvodac.setKratkiOpis(request.getKratkiOpis());
        izvodac.setPrijasnjiPoslovi(request.getPrijasnjiPoslovi());
        izvodac.setRadiOd(request.getRadiOd());
        izvodac.setUkupnoGodinaIskustva(request.getUkupnoGodinaIskustva());

        izvodacRepo.save(izvodac);

        String token = jwtTokenProvider.generateToken(
                izvodac.getUsernameIzvodac(), "IZVODAC");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, izvodac.getUsernameIzvodac(), "IZVODAC"));
    }

    @PostMapping("/register/business")
    public ResponseEntity<AuthResponse> registerBusiness(
            @Valid @RequestBody RegisterBusinessRequest request) {

        if (businessRepo.existsByUsernameBusiness(request.getUsernameBusiness())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username već postoji");
        }

        BusinessKorisnik business = new BusinessKorisnik();
        business.setUsernameBusiness(request.getUsernameBusiness());
        business.setNazivKluba(request.getNazivKluba());
        business.setEmail(request.getEmail());
        business.setLozinka(passwordEncoder.encode(request.getLozinka()));
        business.setLokacija(request.getLokacija());
        business.setOpis(request.getOpis());
        business.setBrojTelefona(request.getBrojTelefona());

        businessRepo.save(business);

        String token = jwtTokenProvider.generateToken(
                business.getUsernameBusiness(), "BUSINESS");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, business.getUsernameBusiness(), "BUSINESS"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        // Prvo probaj naći izvođača
        var izvodacOpt = izvodacRepo.findById(request.getUsername());
        if (izvodacOpt.isPresent()) {
            IzvodacKorisnik izvodac = izvodacOpt.get();
            if (passwordEncoder.matches(request.getPassword(), izvodac.getLozinka())) {
                String token = jwtTokenProvider.generateToken(
                        izvodac.getUsernameIzvodac(), "IZVODAC");
                return ResponseEntity.ok(
                        new AuthResponse(token, izvodac.getUsernameIzvodac(), "IZVODAC"));
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Pogrešna lozinka");
        }

        // Zatim probaj business korisnika
        var businessOpt = businessRepo.findById(request.getUsername());
        if (businessOpt.isPresent()) {
            BusinessKorisnik business = businessOpt.get();
            if (passwordEncoder.matches(request.getPassword(), business.getLozinka())) {
                String token = jwtTokenProvider.generateToken(
                        business.getUsernameBusiness(), "BUSINESS");
                return ResponseEntity.ok(
                        new AuthResponse(token, business.getUsernameBusiness(), "BUSINESS"));
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Pogrešna lozinka");
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Korisnik ne postoji");
    }
}