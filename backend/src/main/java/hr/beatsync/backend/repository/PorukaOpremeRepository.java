package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.PorukaOpreme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PorukaOpremeRepository extends JpaRepository<PorukaOpreme, Integer> {

    List<PorukaOpreme> findByNajam_IdNajmaOrderByTimestampPorukeAsc(Integer najamId);

    boolean existsByNajam_IdNajmaAndMessageType(Integer najamId, String messageType);

    @Query("SELECT p FROM PorukaOpreme p WHERE p.najam.idNajma = :najamId AND p.posiljateljeUsername != :username AND p.readStatus = false")
    List<PorukaOpreme> findUnreadByNajamForUser(@Param("najamId") Integer najamId, @Param("username") String username);

    @Query("SELECT COUNT(p) FROM PorukaOpreme p WHERE (p.najam.vlasnikUsername = :username OR p.najam.najmoprimacUsername = :username) AND p.posiljateljeUsername != :username AND p.readStatus = false")
    long countUnreadForUser(@Param("username") String username);

    // Zadnja CHAT poruka po najmu za inbox prikaz — za vlasnika
    @Query("SELECT p FROM PorukaOpreme p WHERE p.najam.vlasnikUsername = :username AND p.messageType = 'CHAT' AND p.timestampPoruke = (SELECT MAX(p2.timestampPoruke) FROM PorukaOpreme p2 WHERE p2.najam.idNajma = p.najam.idNajma AND p2.messageType = 'CHAT') ORDER BY p.timestampPoruke DESC")
    List<PorukaOpreme> findRazgovoriForVlasnik(@Param("username") String username);

    // Zadnja CHAT poruka po najmu za inbox prikaz — za najmoprimca
    @Query("SELECT p FROM PorukaOpreme p WHERE p.najam.najmoprimacUsername = :username AND p.messageType = 'CHAT' AND p.timestampPoruke = (SELECT MAX(p2.timestampPoruke) FROM PorukaOpreme p2 WHERE p2.najam.idNajma = p.najam.idNajma AND p2.messageType = 'CHAT') ORDER BY p.timestampPoruke DESC")
    List<PorukaOpreme> findRazgovoriForNajmoprimac(@Param("username") String username);

    // Zahtjevi za vlasnika: SYSTEM_NOTIFICATION bez CHAT poruka (novi zahtjevi)
    @Query("""
        SELECT p FROM PorukaOpreme p
        WHERE p.najam.vlasnikUsername = :username
          AND p.messageType = 'SYSTEM_NOTIFICATION'
          AND p.najam.status = hr.beatsync.backend.enums.StatusNajmaOpreme.REQUESTED
          AND NOT EXISTS (
              SELECT p2 FROM PorukaOpreme p2
              WHERE p2.najam.idNajma = p.najam.idNajma AND p2.messageType = 'CHAT'
          )
        ORDER BY p.timestampPoruke DESC
        """)
    List<PorukaOpreme> findZahtjeviForVlasnik(@Param("username") String username);
}
