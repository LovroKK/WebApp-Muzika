package hr.beatsync.backend.repository;

import hr.beatsync.backend.model.IzvodacKorisnik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IzvodacKorisnikRepository extends JpaRepository<IzvodacKorisnik, String> {
    Optional<IzvodacKorisnik> findByUsernameIzvodac(String username);
    Optional<IzvodacKorisnik> findByEmail(String email);
    boolean existsByUsernameIzvodac(String username);

    @Query("SELECT i FROM IzvodacKorisnik i WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(i.ime) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(i.prezime) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(i.usernameIzvodac) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(i.kratkiOpis) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:minCijena IS NULL OR i.cijenaPoSatu >= :minCijena) AND " +
           "(:maxCijena IS NULL OR i.cijenaPoSatu <= :maxCijena)")
    List<IzvodacKorisnik> search(@Param("search") String search,
                                  @Param("minCijena") BigDecimal minCijena,
                                  @Param("maxCijena") BigDecimal maxCijena);
}