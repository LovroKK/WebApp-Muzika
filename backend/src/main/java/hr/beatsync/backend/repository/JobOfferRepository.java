package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Integer> {
    Optional<JobOffer> findByIdPonude(Integer idPonude);
    Optional<JobOffer> findByNazivPonude(String nazivPonude);
    List<JobOffer> findByBusinessPonuda_UsernameBusiness(String usernameBusiness);
    List<JobOffer> findByPopunjenFalse();
}