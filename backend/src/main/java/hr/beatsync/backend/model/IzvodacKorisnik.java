package hr.beatsync.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "izvodac_korisnik")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IzvodacKorisnik {

    @Id
    @Column(name = "username_izvodac", length = 50)
    private String usernameIzvodac;

    @Lob
    @Column(name = "logo_avatar", columnDefinition = "LONGBLOB")
    private byte[] logoAvatar;

    @Column(name = "ime", nullable = false, length = 100)
    private String ime;

    @Column(name = "prezime", nullable = false, length = 100)
    private String prezime;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "broj_telefona", length = 20)
    private String brojTelefona;

    @Column(name = "link_mixtape")
    private String linkMixtape;

    @Column(name = "cijena_po_satu", precision = 8, scale = 2)
    private BigDecimal cijenaPoSatu;

    @Column(name = "kratki_opis", length = 1000)
    private String kratkiOpis;

    @Column(name = "prijasnji_poslovi", length = 5000)
    private String prijasnjiPoslovi;

    @Column(name = "radi_od")
    private Integer radiOd;

    @Column(name = "ukupno_godina_iskustva")
    private Integer ukupnoGodinaIskustva;

    @Column(name = "lozinka", nullable = false, length = 255)
    private String lozinka;

    // Veze
    @OneToMany(mappedBy = "vlasnikOpreme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Oprema> opremaList = new ArrayList<>();

    @OneToMany(mappedBy = "izvodacRezervacija", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rezervacija> rezervacije = new ArrayList<>();

    // Poruke gdje je izvođač sudionik
    @OneToMany(mappedBy = "izvodacPoruka")
    private List<Poruka> poruke = new ArrayList<>();
}