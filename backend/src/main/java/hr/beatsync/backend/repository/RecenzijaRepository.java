package hr.beatsync.backend.repository;

import hr.beatsync.backend.enums.StatusValidacije;
import hr.beatsync.backend.model.Recenzija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecenzijaRepository extends JpaRepository<Recenzija, Integer> {

    List<Recenzija> findByRezervacija_IzvodacRezervacija_UsernameIzvodacAndStatusValidacije(
            String usernameIzvodac, StatusValidacije status);

    List<Recenzija> findByStatusValidacije(StatusValidacije status);

    @Query("SELECT AVG(r.ocjena) AS avg, COUNT(r) AS cnt " +
           "FROM Recenzija r " +
           "WHERE r.rezervacija.izvodacRezervacija.usernameIzvodac = :username " +
           "AND r.statusValidacije = hr.beatsync.backend.enums.StatusValidacije.APPROVED")
    OcjenaAggregate findAggregateForIzvodac(@Param("username") String username);

    interface OcjenaAggregate {
        Double getAvg();
        Long getCnt();
    }
}
