package hr.beatsync.backend.model;

import hr.beatsync.backend.enums.StatusValidacije;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recenzija")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recenzija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recenzije")
    private Integer idRecenzije;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rezervacije", nullable = false, unique = true)
    private Rezervacija rezervacija;

    @Column(name = "ocjena", nullable = false, precision = 2, scale = 1)
    private BigDecimal ocjena;

    @Column(name = "komentar", length = 1000)
    private String komentar;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_validacije", nullable = false)
    private StatusValidacije statusValidacije = StatusValidacije.PENDING;

    @Column(name = "datum_recenzije", nullable = false)
    private LocalDateTime datumRecenzije = LocalDateTime.now();
}