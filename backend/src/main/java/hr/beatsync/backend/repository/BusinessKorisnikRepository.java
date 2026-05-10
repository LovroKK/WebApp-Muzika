package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.BusinessKorisnik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessKorisnikRepository extends JpaRepository<BusinessKorisnik, String> {
    Optional<BusinessKorisnik> findByUsernameBusiness(String username);
    Optional<BusinessKorisnik> findByEmail(String email);
    boolean existsByUsernameBusiness(String username);
}   