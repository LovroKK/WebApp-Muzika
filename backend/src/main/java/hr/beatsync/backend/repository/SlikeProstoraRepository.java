package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.SlikeProstora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlikeProstoraRepository extends JpaRepository<SlikeProstora, Integer> {
}
