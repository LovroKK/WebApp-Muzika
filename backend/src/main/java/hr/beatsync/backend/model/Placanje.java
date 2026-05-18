package hr.beatsync.backend.model;

import hr.beatsync.backend.enums.StatusPlacanja;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "placanje")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Placanje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_placanja")
    private Integer idPlacanja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rezervacije", nullable = false)
    private Rezervacija rezervacija;

    @Column(name = "iznos", nullable = false, precision = 10, scale = 2)
    private BigDecimal iznos;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "eur";

    @Enumerated(EnumType.STRING)
    @Column(name = "status_placanja", nullable = false)
    @Builder.Default
    private StatusPlacanja statusPlacanja = StatusPlacanja.PENDING;

    @Column(name = "stripe_session_id", length = 255)
    private String stripeSessionId;

    @Column(name = "stripe_payment_intent_id", length = 255)
    private String stripePaymentIntentId;

    @Column(name = "datum_kreiranja", nullable = false)
    private LocalDateTime datumKreiranja;

    @Column(name = "datum_placanja")
    private LocalDateTime datumPlacanja;
}
