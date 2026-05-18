package hr.beatsync.backend.service;

import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.model.Rezervacija;
import hr.beatsync.backend.repository.RezervacijaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class RezervacijaScheduler {

    private static final Logger log = LoggerFactory.getLogger(RezervacijaScheduler.class);

    private final RezervacijaRepository rezervacijaRepo;

    public RezervacijaScheduler(RezervacijaRepository rezervacijaRepo) {
        this.rezervacijaRepo = rezervacijaRepo;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void promoteAcceptedToInProgress() {
        LocalDate danas = LocalDate.now();
        LocalTime sada = LocalTime.now();

        List<Rezervacija> kandidati = rezervacijaRepo.findByStatusRezervacije(StatusRezervacije.ACCEPTED);
        int promoted = 0;
        for (Rezervacija r : kandidati) {
            if (r.getJobOffer() != null
                    && !danas.isBefore(r.getJobOffer().getDatum())
                    && !sada.isBefore(r.getJobOffer().getPocetak())) {
                r.setStatusRezervacije(StatusRezervacije.IN_PROGRESS);
                promoted++;
            }
        }

        if (promoted > 0) {
            rezervacijaRepo.saveAll(kandidati);
            log.info("Scheduler: prebačeno {} rezervacija u IN_PROGRESS", promoted);
        }
    }
}
