package hr.beatsync.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorit",
       uniqueConstraints = @UniqueConstraint(columnNames = {"korisnik_username", "tip_favorita", "favorit_username"}))
@Data
@NoArgsConstructor
public class Favorit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "korisnik_username", nullable = false, length = 50)
    private String korisnikUsername;

    @Column(name = "tip_favorita", nullable = false, length = 20)
    private String tipFavorita;

    @Column(name = "favorit_username", nullable = false, length = 50)
    private String favoritUsername;

    public Favorit(String korisnikUsername, String tipFavorita, String favoritUsername) {
        this.korisnikUsername = korisnikUsername;
        this.tipFavorita = tipFavorita;
        this.favoritUsername = favoritUsername;
    }
}
