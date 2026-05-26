package hr.beatsync.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.beatsync.backend.dto.CreateRecenzijaRequest;
import hr.beatsync.backend.dto.RecenzijaResponse;
import hr.beatsync.backend.service.RecenzijaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Funkcijski testovi REST endpointa za recenzije.
 * Servisni sloj je mockiran; provjerava se HTTP ponašanje i autorizacija po ulozi.
 */
@ExtendWith(MockitoExtension.class)
class RecenzijaControllerTest {

    @Mock private RecenzijaService recenzijaService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RecenzijaController controller = new RecenzijaController(recenzijaService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void prijaviSe(String username, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private CreateRecenzijaRequest zahtjev() {
        CreateRecenzijaRequest r = new CreateRecenzijaRequest();
        r.setIdRezervacije(1);
        r.setOcjena(new BigDecimal("4.5"));
        r.setKomentar("Odlična suradnja");
        return r;
    }

    @Test
    @DisplayName("Business korisnik uspješno kreira recenziju → 201")
    void create_business_201() throws Exception {
        RecenzijaResponse resp = new RecenzijaResponse();
        resp.setIdRezervacije(1);
        resp.setOcjena(new BigDecimal("4.5"));
        when(recenzijaService.createReview(eq("klub_x"), any())).thenReturn(resp);
        prijaviSe("klub_x", "BUSINESS");

        mockMvc.perform(post("/api/recenzije")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtjev())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRezervacije").value(1));
    }

    @Test
    @DisplayName("Izvođač ne smije ostaviti recenziju → 403")
    void create_izvodac_zabranjeno_403() throws Exception {
        prijaviSe("dj_x", "IZVODAC");

        mockMvc.perform(post("/api/recenzije")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zahtjev())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Javni dohvat odobrenih recenzija izvođača → 200, lista")
    void listForDj_javno_200() throws Exception {
        RecenzijaResponse resp = new RecenzijaResponse();
        resp.setIdRezervacije(1);
        when(recenzijaService.listApprovedForIzvodac("dj_x")).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/recenzije/dj/dj_x"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
