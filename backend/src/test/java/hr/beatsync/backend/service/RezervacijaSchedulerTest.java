package hr.beatsync.backend.service;

import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.model.JobOffer;
import hr.beatsync.backend.model.Rezervacija;
import hr.beatsync.backend.repository.RezervacijaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Jedinični testovi vremenski okidanog prijelaza statusa rezervacije.
 * Scheduler svakih 60 s prebacuje ACCEPTED rezervacije čiji je termin nastupio u IN_PROGRESS.
 */
@ExtendWith(MockitoExtension.class)
class RezervacijaSchedulerTest {

    @Mock private RezervacijaRepository rezervacijaRepo;
    @InjectMocks private RezervacijaScheduler scheduler;

    private Rezervacija acceptedSaJobOfferom(LocalDate datum, LocalTime pocetak) {
        JobOffer jo = JobOffer.builder()
                .nazivPonude("Nastup")
                .datum(datum)
                .pocetak(pocetak)
                .kraj(pocetak.plusHours(2))
                .build();
        return Rezervacija.builder()
                .idRezervacije(1)
                .statusRezervacije(StatusRezervacije.ACCEPTED)
                .jobOffer(jo)
                .build();
    }

    @Test
    @DisplayName("ACCEPTED rezervacija čiji je termin nastupio prelazi u IN_PROGRESS")
    void promovira_kadJeTerminNastupio() {
        Rezervacija rez = acceptedSaJobOfferom(LocalDate.now(), LocalTime.MIDNIGHT);
        when(rezervacijaRepo.findByStatusRezervacije(StatusRezervacije.ACCEPTED))
                .thenReturn(new ArrayList<>(List.of(rez)));

        scheduler.promoteAcceptedToInProgress();

        assertEquals(StatusRezervacije.IN_PROGRESS, rez.getStatusRezervacije());
        verify(rezervacijaRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("ACCEPTED rezervacija s budućim terminom ostaje ACCEPTED")
    void neProvodi_kadJeTerminUBuducnosti() {
        Rezervacija rez = acceptedSaJobOfferom(LocalDate.now().plusDays(1), LocalTime.NOON);
        when(rezervacijaRepo.findByStatusRezervacije(StatusRezervacije.ACCEPTED))
                .thenReturn(new ArrayList<>(List.of(rez)));

        scheduler.promoteAcceptedToInProgress();

        assertEquals(StatusRezervacije.ACCEPTED, rez.getStatusRezervacije());
        verify(rezervacijaRepo, never()).saveAll(anyList());
    }
}
