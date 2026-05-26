package hr.beatsync.backend.service;

import hr.beatsync.backend.dto.CreateRecenzijaRequest;
import hr.beatsync.backend.dto.RecenzijaResponse;
import hr.beatsync.backend.enums.StatusPlacanja;
import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.enums.StatusValidacije;
import hr.beatsync.backend.model.BusinessKorisnik;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.model.Recenzija;
import hr.beatsync.backend.model.Rezervacija;
import hr.beatsync.backend.repository.PlacanjeRepository;
import hr.beatsync.backend.repository.RecenzijaRepository;
import hr.beatsync.backend.repository.RezervacijaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Jedinični (unit) testovi poslovne logike za ostavljanje recenzije.
 * Repozitoriji su mockirani (Mockito) — test ne ovisi o bazi podataka.
 */
@ExtendWith(MockitoExtension.class)
class RecenzijaServiceTest {

    @Mock private RecenzijaRepository recenzijaRepo;
    @Mock private RezervacijaRepository rezervacijaRepo;
    @Mock private PlacanjeRepository placanjeRepo;

    @InjectMocks private RecenzijaService recenzijaService;

    // --- Pomoćne metode za pripremu podataka ---

    private Rezervacija rezervacija(StatusRezervacije status) {
        BusinessKorisnik biz = BusinessKorisnik.builder()
                .usernameBusiness("klub_x").nazivKluba("Klub X").build();
        IzvodacKorisnik izv = IzvodacKorisnik.builder()
                .usernameIzvodac("dj_x").ime("Ivan").prezime("Horvat").build();
        return Rezervacija.builder()
                .idRezervacije(1)
                .statusRezervacije(status)
                .businessRezervacija(biz)
                .izvodacRezervacija(izv)
                .build();
    }

    private CreateRecenzijaRequest zahtjev() {
        CreateRecenzijaRequest r = new CreateRecenzijaRequest();
        r.setIdRezervacije(1);
        r.setOcjena(new BigDecimal("4.5"));
        r.setKomentar("Odlična suradnja");
        return r;
    }

    // --- Testni slučajevi ---

    @Test
    @DisplayName("Uspješno kreiranje recenzije postavlja status APPROVED i sprema zapis")
    void createReview_happyPath_spremaApproved() {
        when(rezervacijaRepo.findByIdRezervacije(1))
                .thenReturn(Optional.of(rezervacija(StatusRezervacije.COMPLETED)));
        when(placanjeRepo.existsByRezervacija_IdRezervacijeAndStatusPlacanja(1, StatusPlacanja.PAID))
                .thenReturn(true);
        when(recenzijaRepo.existsById(1)).thenReturn(false);
        when(recenzijaRepo.save(any(Recenzija.class))).thenAnswer(inv -> inv.getArgument(0));

        RecenzijaResponse resp = recenzijaService.createReview("klub_x", zahtjev());

        assertEquals(new BigDecimal("4.5"), resp.getOcjena());

        ArgumentCaptor<Recenzija> captor = ArgumentCaptor.forClass(Recenzija.class);
        verify(recenzijaRepo).save(captor.capture());
        assertEquals(StatusValidacije.APPROVED, captor.getValue().getStatusValidacije());
    }

    @Test
    @DisplayName("Nepostojeća rezervacija vraća 404 Not Found")
    void createReview_rezervacijaNePostoji_404() {
        when(rezervacijaRepo.findByIdRezervacije(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> recenzijaService.createReview("klub_x", zahtjev()));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Recenziju ne smije ostaviti korisnik koji nije vlasnik rezervacije (403)")
    void createReview_nijeVlasnik_403() {
        when(rezervacijaRepo.findByIdRezervacije(1))
                .thenReturn(Optional.of(rezervacija(StatusRezervacije.COMPLETED)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> recenzijaService.createReview("drugi_klub", zahtjev()));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Recenzija nije moguća dok rezervacija nije COMPLETED (400)")
    void createReview_nijeCompleted_400() {
        when(rezervacijaRepo.findByIdRezervacije(1))
                .thenReturn(Optional.of(rezervacija(StatusRezervacije.ACCEPTED)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> recenzijaService.createReview("klub_x", zahtjev()));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Recenzija nije moguća bez završenog plaćanja (400)")
    void createReview_nijePlaceno_400() {
        when(rezervacijaRepo.findByIdRezervacije(1))
                .thenReturn(Optional.of(rezervacija(StatusRezervacije.COMPLETED)));
        when(placanjeRepo.existsByRezervacija_IdRezervacijeAndStatusPlacanja(1, StatusPlacanja.PAID))
                .thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> recenzijaService.createReview("klub_x", zahtjev()));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Dupla recenzija za istu rezervaciju vraća 409 Conflict")
    void createReview_recenzijaVecPostoji_409() {
        when(rezervacijaRepo.findByIdRezervacije(1))
                .thenReturn(Optional.of(rezervacija(StatusRezervacije.COMPLETED)));
        when(placanjeRepo.existsByRezervacija_IdRezervacijeAndStatusPlacanja(1, StatusPlacanja.PAID))
                .thenReturn(true);
        when(recenzijaRepo.existsById(1)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> recenzijaService.createReview("klub_x", zahtjev()));
        assertEquals(409, ex.getStatusCode().value());
    }
}
