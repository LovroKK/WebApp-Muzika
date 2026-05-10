package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.Rezervacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RezervacijaRepository extends JpaRepository<Rezervacija, Integer> {
    Optional<Rezervacija> findByIdRezervacije(Integer idRezervacije);
}
