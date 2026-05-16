package hr.beatsync.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "slike_prostora")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlikeProstora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_slike")
    private Integer idSlike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username_business", nullable = false)
    private BusinessKorisnik businessSlike;

    @Lob
    @Column(name = "slika_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] slikaData;

    @Column(name = "opis_slike", length = 255)
    private String opisSlike;
}