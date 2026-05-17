package hr.beatsync.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_offer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ponude")
    private Integer idPonude;

    @Column(name = "naziv_ponude", nullable = false, length = 255)
    private String nazivPonude;

    @Column(name = "datum", nullable = false)
    private LocalDate datum;

    @Column(name = "budzet", precision = 10, scale = 2)
    private BigDecimal budzet;

    @Column(name = "pocetak", nullable = false)
    private LocalTime pocetak;

    @Column(name = "kraj", nullable = false)
    private LocalTime kraj;

    @Column(name = "lokacija", length = 255)
    private String lokacija;

    @Column(name = "opis_posla", length = 1000)
    private String opisPosla;

    @Column(name = "potrebno_iskustvo", length = 500)
    private String potrebnoIskustvo;

    @Column(name = "zanrovi", length = 500)
    private String zanrovi;

    @Column(name = "oprema_zahtjevi", length = 500)
    private String opremaZahtjevi;

    @Column(name = "ostalo_oprema", length = 500)
    private String ostaloOprema;

    @Column(name = "popunjen", nullable = false)
    @Builder.Default
    private Boolean popunjen = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username_business", nullable = false)
    private BusinessKorisnik businessPonuda;

    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rezervacija> rezervacije = new ArrayList<>();
}