package hr.beatsync.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.beatsync.backend.dto.LoginRequest;
import hr.beatsync.backend.dto.RegisterBusinessRequest;
import hr.beatsync.backend.dto.RegisterIzvodacRequest;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.repository.BusinessKorisnikRepository;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import hr.beatsync.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Funkcijski (enduser) testovi REST endpointa za registraciju i prijavu.
 * Koristi se MockMvc standalone — pravi HTTP zahtjevi na kontroler bez baze i Spring konteksta.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private IzvodacKorisnikRepository izvodacRepo;
    @Mock private BusinessKorisnikRepository businessRepo;
    @Mock private JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(izvodacRepo, businessRepo, encoder, jwtTokenProvider);
        // @Value polja se u standalone modu ne injektiraju automatski.
        ReflectionTestUtils.setField(controller, "adminUsername", "admin");
        ReflectionTestUtils.setField(controller, "adminPassword", encoder.encode("adminpass"));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private RegisterIzvodacRequest validanIzvodac() {
        return new RegisterIzvodacRequest(
                "dj_x", "Ivan", "Horvat", "ivan@example.com", "0911234567",
                "tajna123", "http://soundcloud.com/dj_x", new BigDecimal("100.00"),
                "Kratki opis", "Prijašnji poslovi", 2018, 5);
    }

    @Test
    @DisplayName("Registracija izvođača s valjanim podacima vraća 201 i token")
    void registerIzvodac_valjan_201() throws Exception {
        when(izvodacRepo.existsByUsernameIzvodac("dj_x")).thenReturn(false);
        when(jwtTokenProvider.generateToken("dj_x", "IZVODAC")).thenReturn("tok123");

        mockMvc.perform(post("/api/auth/register/izvodac")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validanIzvodac())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("IZVODAC"))
                .andExpect(jsonPath("$.token").value("tok123"));
    }

    @Test
    @DisplayName("Registracija s već zauzetim korisničkim imenom vraća 409")
    void registerBusiness_zauzetUsername_409() throws Exception {
        when(businessRepo.existsByUsernameBusiness("klub_x")).thenReturn(true);

        RegisterBusinessRequest req = new RegisterBusinessRequest(
                "klub_x", "Klub X", "klub@example.com", "tajna123",
                "Rijeka", "Opis kluba", "051123456");

        mockMvc.perform(post("/api/auth/register/business")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Prijava s ispravnim podacima vraća 200 i ulogu IZVODAC")
    void login_ispravan_200() throws Exception {
        IzvodacKorisnik izvodac = IzvodacKorisnik.builder()
                .usernameIzvodac("dj_x").lozinka(encoder.encode("pass123")).build();
        when(izvodacRepo.findByUsernameExact("dj_x")).thenReturn(Optional.of(izvodac));
        when(jwtTokenProvider.generateToken("dj_x", "IZVODAC")).thenReturn("tok");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("dj_x", "pass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("IZVODAC"));
    }

    @Test
    @DisplayName("Prijava s krivom lozinkom vraća 401")
    void login_krivaLozinka_401() throws Exception {
        IzvodacKorisnik izvodac = IzvodacKorisnik.builder()
                .usernameIzvodac("dj_x").lozinka(encoder.encode("pass123")).build();
        when(izvodacRepo.findByUsernameExact("dj_x")).thenReturn(Optional.of(izvodac));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("dj_x", "krivo"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Prijava nepostojećeg korisnika vraća 404")
    void login_nepostojeci_404() throws Exception {
        when(izvodacRepo.findByUsernameExact("ghost")).thenReturn(Optional.empty());
        when(businessRepo.findByUsernameExact("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("ghost", "x"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Registracija s nevaljanim (praznim) podacima vraća 400")
    void registerIzvodac_nevaljan_400() throws Exception {
        RegisterIzvodacRequest prazan = new RegisterIzvodacRequest();

        mockMvc.perform(post("/api/auth/register/izvodac")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prazan)))
                .andExpect(status().isBadRequest());
    }
}
