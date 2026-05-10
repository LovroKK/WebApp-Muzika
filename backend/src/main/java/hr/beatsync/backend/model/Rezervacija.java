package hr.beatsync.backend.model;

import hr.beatsync.backend.enums.StatusRezervacije;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rezervacije")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rezervacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rezervacije")
    private Integer idRezervacije;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ponude")
    private JobOffer jobOffer;

    @Column(name = "period_od", nullable = false)
    private LocalDateTime periodOd;

    @Column(name = "period_do", nullable = false)
    private LocalDateTime periodDo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_rezervacije", nullable = false)
    private StatusRezervacije statusRezervacije = StatusRezervacije.REQUESTED;

    @Column(name = "potvrda_rezervacije", nullable = false)
    private Boolean potvrdaRezervacije = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username_izvodac", nullable = false)
    private IzvodacKorisnik izvodacRezervacija;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username_business", nullable = false)
    private BusinessKorisnik businessRezervacija;

    @OneToOne(mappedBy = "rezervacija", cascade = CascadeType.ALL)
    private Recenzija recenzija;
}