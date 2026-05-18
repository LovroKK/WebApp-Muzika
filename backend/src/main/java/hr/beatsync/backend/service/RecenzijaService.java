package hr.beatsync.backend.service;

import hr.beatsync.backend.dto.CreateRecenzijaRequest;
import hr.beatsync.backend.dto.RecenzijaAdminResponse;
import hr.beatsync.backend.dto.RecenzijaResponse;
import hr.beatsync.backend.enums.StatusPlacanja;
import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.enums.StatusValidacije;
import hr.beatsync.backend.model.Recenzija;
import hr.beatsync.backend.model.Rezervacija;
import hr.beatsync.backend.repository.PlacanjeRepository;
import hr.beatsync.backend.repository.RecenzijaRepository;
import hr.beatsync.backend.repository.RezervacijaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecenzijaService {

    private final RecenzijaRepository recenzijaRepo;
    private final RezervacijaRepository rezervacijaRepo;
    private final PlacanjeRepository placanjeRepo;

    public RecenzijaService(RecenzijaRepository recenzijaRepo,
                            RezervacijaRepository rezervacijaRepo,
                            PlacanjeRepository placanjeRepo) {
        this.recenzijaRepo = recenzijaRepo;
        this.rezervacijaRepo = rezervacijaRepo;
        this.placanjeRepo = placanjeRepo;
    }

    @Transactional
    public RecenzijaResponse createReview(String businessUsername, CreateRecenzijaRequest req) {
        Rezervacija rez = rezervacijaRepo.findByIdRezervacije(req.getIdRezervacije())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervacija ne postoji"));

        if (!rez.getBusinessRezervacija().getUsernameBusiness().equals(businessUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pristup ovoj rezervaciji");
        }

        if (rez.getStatusRezervacije() != StatusRezervacije.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Recenziju je moguće ostaviti tek nakon završene rezervacije");
        }

        if (!placanjeRepo.existsByRezervacija_IdRezervacijeAndStatusPlacanja(
                rez.getIdRezervacije(), StatusPlacanja.PAID)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Plaćanje mora biti završeno prije ostavljanja recenzije");
        }

        if (recenzijaRepo.existsById(rez.getIdRezervacije())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Recenzija za ovu rezervaciju već postoji");
        }

        Recenzija novo = Recenzija.builder()
                .rezervacija(rez)
                .ocjena(req.getOcjena())
                .komentar(req.getKomentar())
                .statusValidacije(StatusValidacije.APPROVED)
                .datumRecenzije(LocalDateTime.now())
                .build();

        try {
            Recenzija saved = recenzijaRepo.save(novo);
            return RecenzijaResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Recenzija za ovu rezervaciju već postoji");
        }
    }

    public List<RecenzijaResponse> listApprovedForIzvodac(String usernameIzvodac) {
        return recenzijaRepo
                .findByRezervacija_IzvodacRezervacija_UsernameIzvodacAndStatusValidacije(
                        usernameIzvodac, StatusValidacije.APPROVED)
                .stream()
                .map(RecenzijaResponse::from)
                .toList();
    }

    public List<RecenzijaAdminResponse> listAllForAdmin(StatusValidacije filter) {
        List<Recenzija> sve = (filter == null)
                ? recenzijaRepo.findAll()
                : recenzijaRepo.findByStatusValidacije(filter);
        return sve.stream()
                .sorted((a, b) -> b.getDatumRecenzije().compareTo(a.getDatumRecenzije()))
                .map(RecenzijaAdminResponse::from)
                .toList();
    }

    @Transactional
    public RecenzijaAdminResponse changeStatus(Integer idRezervacije, StatusValidacije noviStatus) {
        Recenzija r = recenzijaRepo.findById(idRezervacije)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recenzija ne postoji"));
        r.setStatusValidacije(noviStatus);
        recenzijaRepo.save(r);
        return RecenzijaAdminResponse.from(r);
    }

    @Transactional
    public void deleteReview(Integer idRezervacije) {
        if (!recenzijaRepo.existsById(idRezervacije)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recenzija ne postoji");
        }
        recenzijaRepo.deleteById(idRezervacije);
    }
}
