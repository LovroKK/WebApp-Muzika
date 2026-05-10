package hr.beatsync.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "business_korisnik")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessKorisnik {

    @Id
    @Column(name = "username_business", length = 50)
    private String usernameBusiness;

    @Column(name = "logo_avatar")
    private String logoAvatar;

    @Column(name = "naziv_kluba", nullable = false, length = 255)
    private String nazivKluba;

    @Column(name = "lokacija", length = 255)
    private String lokacija;

    @Column(name = "opis", length = 5000)
    private String opis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_dj", referencedColumnName = "username_izvodac")
    private IzvodacKorisnik residentDj;

    @Column(name = "najbolji_izvodaci", length = 1000)
    private String najboljiIzvodaci;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "broj_telefona", length = 20)
    private String brojTelefona;

    @Column(name = "lozinka", nullable = false, length = 255)
    private String lozinka;

    // Veze
    @OneToMany(mappedBy = "businessPonuda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobOffer> jobOffers = new ArrayList<>();

    @OneToMany(mappedBy = "businessRezervacija", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rezervacija> rezervacije = new ArrayList<>();

    @OneToMany(mappedBy = "businessPoruka")
    private List<Poruka> poruke = new ArrayList<>();

    @OneToMany(mappedBy = "businessSlike", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SlikeProstora> slikeProstora = new ArrayList<>();
}