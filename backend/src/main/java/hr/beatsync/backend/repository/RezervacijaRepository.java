package hr.beatsync.backend.repository;

import hr.beatsync.backend.enums.StatusRezervacije;
import hr.beatsync.backend.model.Rezervacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RezervacijaRepository extends JpaRepository<Rezervacija, Integer> {
    Optional<Rezervacija> findByIdRezervacije(Integer idRezervacije);
    boolean existsByJobOffer_IdPonudeAndIzvodacRezervacija_UsernameIzvodac(Integer idPonude, String usernameIzvodac);
    boolean existsByJobOffer_IdPonudeAndIzvodacRezervacija_UsernameIzvodacAndStatusRezervacijeIn(Integer idPonude, String usernameIzvodac, List<StatusRezervacije> statusi);
    List<Rezervacija> findByIzvodacRezervacija_UsernameIzvodac(String usernameIzvodac);
    List<Rezervacija> findByBusinessRezervacija_UsernameBusiness(String usernameBusiness);
    long countByJobOffer_IdPonudeAndStatusRezervacije(Integer idPonude, StatusRezervacije status);
    List<Rezervacija> findByStatusRezervacije(StatusRezervacije status);
}
