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
    @Column(name = "id_rezervacije")
    private Integer idRezervacije;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_rezervacije")
    private Rezervacija rezervacija;

    @Column(name = "ocjena", nullable = false, precision = 2, scale = 1)
    private BigDecimal ocjena;

    @Column(name = "komentar", length = 1000)
    private String komentar;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_validacije", nullable = false)
    private StatusValidacije statusValidacije = StatusValidacije.APPROVED;

    @Column(name = "datum_recenzije", nullable = false)
    private LocalDateTime datumRecenzije = LocalDateTime.now();
}