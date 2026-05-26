package hr.beatsync.backend.controller;

import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.model.BusinessKorisnik;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.model.Rezervacija;
import hr.beatsync.backend.repository.JobOfferRepository;
import hr.beatsync.backend.repository.PlacanjeRepository;
import hr.beatsync.backend.repository.PorukaRepository;
import hr.beatsync.backend.repository.RezervacijaRepository;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Funkcijski testovi REST endpointa za upravljanje rezervacijama (booking tijek).
 * MockMvc standalone; prijavljeni korisnik simulira se preko SecurityContextHoldera.
 */
@ExtendWith(MockitoExtension.class)
class RezervacijaControllerTest {

    @Mock private RezervacijaRepository rezervacijaRepo;
    @Mock private JobOfferRepository jobOfferRepo;
    @Mock private PorukaRepository porukaRepo;
    @Mock private PlacanjeRepository placanjeRepo;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RezervacijaController controller =
                new RezervacijaController(rezervacijaRepo, jobOfferRepo, porukaRepo, placanjeRepo);
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

    private Rezervacija rezervacija(StatusRezervacije status, boolean potvrdaBusiness, boolean potvrdaIzvodac) {
        BusinessKorisnik biz = BusinessKorisnik.builder()
                .usernameBusiness("klub_x").nazivKluba("Klub X").build();
        IzvodacKorisnik izv = IzvodacKorisnik.builder()
                .usernameIzvodac("dj_x").ime("Ivan").prezime("Horvat").build();
        return Rezervacija.builder()
                .idRezervacije(1)
                .statusRezervacije(status)
                .potvrdaBusiness(potvrdaBusiness)
                .potvrdaIzvodac(potvrdaIzvodac)
                .businessRezervacija(biz)
                .izvodacRezervacija(izv)
                .build();
    }

    @Test
    @DisplayName("Business potvrđuje REQUESTED rezervaciju → 200, potvrdaBusiness=true")
    void potvrdi_business_postavljaPotvrdu() throws Exception {
        Rezervacija rez = rezervacija(StatusRezervacije.REQUESTED, false, false);
        when(rezervacijaRepo.findByIdRezervacije(1)).thenReturn(Optional.of(rez));
        when(rezervacijaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(placanjeRepo.findTopByRezervacija_IdRezervacijeOrderByDatumKreiranjaDesc(anyInt()))
                .thenReturn(Optional.empty());
        prijaviSe("klub_x", "BUSINESS");

        mockMvc.perform(put("/api/rezervacije/1/potvrdi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.potvrdaBusiness").value(true))
                .andExpect(jsonPath("$.statusRezervacije").value("REQUESTED"));
    }

    @Test
    @DisplayName("Izvođač potvrđuje kad je business već potvrdio → status ACCEPTED")
    void potvrdi_izvodac_obostranaPotvrda_accepted() throws Exception {
        Rezervacija rez = rezervacija(StatusRezervacije.REQUESTED, true, false);
        when(rezervacijaRepo.findByIdRezervacije(1)).thenReturn(Optional.of(rez));
        when(rezervacijaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(placanjeRepo.findTopByRezervacija_IdRezervacijeOrderByDatumKreiranjaDesc(anyInt()))
                .thenReturn(Optional.empty());
        prijaviSe("dj_x", "IZVODAC");

        mockMvc.perform(put("/api/rezervacije/1/potvrdi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusRezervacije").value("ACCEPTED"));
    }

    @Test
    @DisplayName("Otkazivanje REQUESTED rezervacije → 200, status CANCELLED")
    void otkazi_postavljaCancelled() throws Exception {
        Rezervacija rez = rezervacija(StatusRezervacije.REQUESTED, false, false);
        when(rezervacijaRepo.findByIdRezervacije(1)).thenReturn(Optional.of(rez));
        when(rezervacijaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(porukaRepo.findUnreadByRezervacijaForBusiness(1)).thenReturn(List.of());
        when(placanjeRepo.findTopByRezervacija_IdRezervacijeOrderByDatumKreiranjaDesc(anyInt()))
                .thenReturn(Optional.empty());
        prijaviSe("klub_x", "BUSINESS");

        mockMvc.perform(put("/api/rezervacije/1/otkazi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusRezervacije").value("CANCELLED"));
    }

    @Test
    @DisplayName("Izvođač ne smije označiti rezervaciju završenom → 403")
    void zavrsi_izvodac_zabranjeno_403() throws Exception {
        prijaviSe("dj_x", "IZVODAC");

        mockMvc.perform(put("/api/rezervacije/1/zavrsi"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dohvat rezervacija prijavljenog business korisnika → 200, lista")
    void list_vracaRezervacije() throws Exception {
        Rezervacija rez = rezervacija(StatusRezervacije.ACCEPTED, true, true);
        when(rezervacijaRepo.findByBusinessRezervacija_UsernameBusiness("klub_x"))
                .thenReturn(List.of(rez));
        when(placanjeRepo.findTopByRezervacija_IdRezervacijeOrderByDatumKreiranjaDesc(anyInt()))
                .thenReturn(Optional.empty());
        prijaviSe("klub_x", "BUSINESS");

        mockMvc.perform(get("/api/rezervacije"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].statusRezervacije").value("ACCEPTED"));
    }
}
