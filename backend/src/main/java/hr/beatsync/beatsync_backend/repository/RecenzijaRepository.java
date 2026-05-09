package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.Recenzija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecenzijaRepository extends JpaRepository<Recenzija, Integer> {
}