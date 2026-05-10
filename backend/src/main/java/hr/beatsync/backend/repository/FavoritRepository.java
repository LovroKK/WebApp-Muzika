package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.Favorit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritRepository extends JpaRepository<Favorit, Long> {
    List<Favorit> findByKorisnikUsername(String korisnikUsername);
    List<Favorit> findByKorisnikUsernameAndTipFavorita(String korisnikUsername, String tipFavorita);
    Optional<Favorit> findByKorisnikUsernameAndTipFavoritaAndFavoritUsername(String korisnikUsername, String tipFavorita, String favoritUsername);
    boolean existsByKorisnikUsernameAndTipFavoritaAndFavoritUsername(String korisnikUsername, String tipFavorita, String favoritUsername);
}
