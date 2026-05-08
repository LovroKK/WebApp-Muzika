package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.OpremaLokacije;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OpremaLokacijaRepository extends JpaRepository<OpremaLokacije, Integer> {
    Optional<OpremaLokacije> findById(Integer id);
}