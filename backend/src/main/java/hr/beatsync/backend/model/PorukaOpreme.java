package hr.beatsync.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "poruka_opreme")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PorukaOpreme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_poruke_opreme")
    private Integer idPorukeOpreme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "najam_id", nullable = false)
    private NajamOpreme najam;

    @Column(name = "sadrzaj_poruke", nullable = false, length = 5000)
    private String sadrzajPoruke;

    @Column(name = "timestamp_poruke", nullable = false)
    @Builder.Default
    private LocalDateTime timestampPoruke = LocalDateTime.now();

    @Column(name = "posiljatelj_username", nullable = false, length = 50)
    private String posiljateljeUsername;

    @Column(name = "read_status", nullable = false)
    @Builder.Default
    private Boolean readStatus = false;

    @Column(name = "message_type", nullable = false, length = 20)
    @Builder.Default
    private String messageType = "CHAT";
}
