package hr.beatsync.backend.model;

import hr.beatsync.backend.enums.StatusNajmaOpreme;
import hr.beatsync.backend.enums.TipKorisnika;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "najam_opreme")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NajamOpreme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_najma")
    private Integer idNajma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oprema_id", nullable = false)
    private Oprema oprema;

    @Column(name = "vlasnik_username", nullable = false, length = 50)
    private String vlasnikUsername;

    @Column(name = "najmoprimac_username", nullable = false, length = 50)
    private String najmoprimacUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "najmoprimac_tip", nullable = false)
    private TipKorisnika najmoprimacTip;

    @Column(name = "period_od", nullable = false)
    private LocalDate periodOd;

    @Column(name = "period_do", nullable = false)
    private LocalDate periodDo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private StatusNajmaOpreme status = StatusNajmaOpreme.REQUESTED;

    @Column(name = "timestamp_zahtjeva", nullable = false)
    @Builder.Default
    private LocalDateTime timestampZahtjeva = LocalDateTime.now();

    @Column(name = "napomena", length = 2000)
    private String napomena;
}
