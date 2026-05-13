package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.BusinessKorisnik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface BusinessKorisnikRepository extends JpaRepository<BusinessKorisnik, String> {
    Optional<BusinessKorisnik> findByUsernameBusiness(String username);
    Optional<BusinessKorisnik> findByEmail(String email);
    boolean existsByUsernameBusiness(String username);

    @Query("SELECT b FROM BusinessKorisnik b WHERE BINARY(b.usernameBusiness) = BINARY(:username)")
    Optional<BusinessKorisnik> findByUsernameExact(@Param("username") String username);
}   