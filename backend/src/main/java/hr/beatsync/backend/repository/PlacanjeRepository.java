package hr.beatsync.backend.repository;

import hr.beatsync.backend.enums.StatusPlacanja;
import hr.beatsync.backend.model.Placanje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlacanjeRepository extends JpaRepository<Placanje, Integer> {

    Optional<Placanje> findByStripeSessionId(String stripeSessionId);

    Optional<Placanje> findTopByRezervacija_IdRezervacijeOrderByDatumKreiranjaDesc(Integer idRezervacije);

    boolean existsByRezervacija_IdRezervacijeAndStatusPlacanja(Integer idRezervacije, StatusPlacanja status);
}
