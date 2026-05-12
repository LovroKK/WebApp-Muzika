package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.Oprema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpremaRepository extends JpaRepository<Oprema, Integer> {
    Optional<Oprema> findByIdOpreme(Integer idOpreme);
    Optional<Oprema> findByNazivOpreme(String nazivOpreme);

    @Query("SELECT DISTINCT o FROM Oprema o JOIN FETCH o.vlasnikOpreme LEFT JOIN FETCH o.lokacije ORDER BY o.idOpreme DESC")
    List<Oprema> findAllWithDetailsOrderByIdOpremeDesc();
}