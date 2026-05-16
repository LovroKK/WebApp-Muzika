package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.Poruka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PorukaRepository extends JpaRepository<Poruka, Integer> {

    Optional<Poruka> findByIdPoruke(Integer idPoruke);

    List<Poruka> findByIzvodacPoruka_UsernameIzvodacAndBusinessPoruka_UsernameBusinessOrderByTimestampPorukeAsc(
            String usernameIzvodac, String usernameBusiness);

    List<Poruka> findByIzvodacPoruka_UsernameIzvodacAndBusinessPoruka_UsernameBusinessAndIdRezervacijeOrderByTimestampPorukeAsc(
            String usernameIzvodac, String usernameBusiness, Integer idRezervacije);

    // Unread count for BUSINESS: poruke koje je IZVODAC poslao, a business nije pročitao
    @Query("SELECT COUNT(p) FROM Poruka p WHERE p.businessPoruka.usernameBusiness = :username AND p.readStatus = false AND p.posiljatelj = hr.beatsync.backend.enums.VrstaPosiljatelja.IZVODAC")
    long countUnreadForBusiness(@Param("username") String username);

    // Unread count for IZVODAC: poruke koje je BUSINESS poslao, a izvodac nije pročitao
    @Query("SELECT COUNT(p) FROM Poruka p WHERE p.izvodacPoruka.usernameIzvodac = :username AND p.readStatus = false AND p.posiljatelj = hr.beatsync.backend.enums.VrstaPosiljatelja.BUSINESS")
    long countUnreadForIzvodac(@Param("username") String username);

    // Zahtjevi tab: SYSTEM_NOTIFICATION za business gdje za tu rezervaciju nema CHAT poruka
    @Query("SELECT p FROM Poruka p WHERE p.businessPoruka.usernameBusiness = :username AND p.messageType = 'SYSTEM_NOTIFICATION' AND NOT EXISTS (SELECT p2 FROM Poruka p2 WHERE p2.idRezervacije = p.idRezervacije AND p2.messageType = 'CHAT') ORDER BY p.timestampPoruke DESC")
    List<Poruka> findZahtjeviForBusiness(@Param("username") String username);

    // Razgovori tab: zadnja CHAT poruka po rezervaciji za business
    @Query("SELECT p FROM Poruka p WHERE p.businessPoruka.usernameBusiness = :username AND p.messageType = 'CHAT' AND p.timestampPoruke = (SELECT MAX(p2.timestampPoruke) FROM Poruka p2 WHERE p2.idRezervacije = p.idRezervacije AND p2.messageType = 'CHAT') ORDER BY p.timestampPoruke DESC")
    List<Poruka> findRazgovoriForBusiness(@Param("username") String username);

    // Razgovori za IZVODAC: zadnja CHAT poruka po rezervaciji
    @Query("SELECT p FROM Poruka p WHERE p.izvodacPoruka.usernameIzvodac = :username AND p.messageType = 'CHAT' AND p.timestampPoruke = (SELECT MAX(p2.timestampPoruke) FROM Poruka p2 WHERE p2.idRezervacije = p.idRezervacije AND p2.messageType = 'CHAT') ORDER BY p.timestampPoruke DESC")
    List<Poruka> findRazgovoriForIzvodac(@Param("username") String username);

    // Provjera postoji li barem jedna CHAT poruka za rezervaciju (za otvori-chat logiku)
    boolean existsByIdRezervacijeAndMessageType(Integer idRezervacije, String messageType);

    // Sve poruke za rezervaciju (i CHAT i SYSTEM_NOTIFICATION), sortirano po vremenu
    List<Poruka> findByIdRezervacijeOrderByTimestampPorukeAsc(Integer idRezervacije);

    // Mark all messages as read for a specific conversation for BUSINESS
    @Query("SELECT p FROM Poruka p WHERE p.idRezervacije = :rezId AND p.posiljatelj = hr.beatsync.backend.enums.VrstaPosiljatelja.IZVODAC AND p.readStatus = false")
    List<Poruka> findUnreadByRezervacijaForBusiness(@Param("rezId") Integer rezId);

    // Mark all messages as read for a specific conversation for IZVODAC
    @Query("SELECT p FROM Poruka p WHERE p.idRezervacije = :rezId AND p.posiljatelj = hr.beatsync.backend.enums.VrstaPosiljatelja.BUSINESS AND p.readStatus = false")
    List<Poruka> findUnreadByRezervacijaForIzvodac(@Param("rezId") Integer rezId);
}
