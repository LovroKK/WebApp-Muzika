package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.IzvodacKorisnik;    
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IzvodacKorisnikRepository extends JpaRepository<IzvodacKorisnik, String> {
    Optional<IzvodacKorisnik> findByUsernameIzvodac(String username);
    Optional<IzvodacKorisnik> findByEmail(String email);
    boolean existsByUsernameIzvodac(String username);
}