package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.NajamOpreme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NajamOpremeRepository extends JpaRepository<NajamOpreme, Integer> {

    Optional<NajamOpreme> findByIdNajma(Integer idNajma);

    List<NajamOpreme> findByVlasnikUsernameOrderByTimestampZahtjevaDesc(String vlasnikUsername);

    List<NajamOpreme> findByNajmoprimacUsernameOrderByTimestampZahtjevaDesc(String najmoprimacUsername);
}
