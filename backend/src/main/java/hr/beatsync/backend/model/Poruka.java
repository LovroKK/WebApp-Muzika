package hr.beatsync.backend.model;

import hr.beatsync.backend.enums.VrstaPosiljatelja;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "poruka")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poruka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_poruke")
    private Integer idPoruke;

    @Column(name = "timestamp_poruke", nullable = false)
    private LocalDateTime timestampPoruke = LocalDateTime.now();

    @Column(name = "sadrzaj_poruke", nullable = false, length = 5000)
    private String sadrzajPoruke;

    @Column(name = "read_status", nullable = false)
    private Boolean readStatus = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "posiljatelj", nullable = false)
    private VrstaPosiljatelja posiljatelj;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username_izvodac", nullable = false)
    private IzvodacKorisnik izvodacPoruka;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username_business", nullable = false)
    private BusinessKorisnik businessPoruka;
}