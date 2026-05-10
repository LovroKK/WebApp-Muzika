package hr.beatsync.backend.model;

import hr.beatsync.backend.enums.KategorijaOpreme;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "oprema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oprema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opreme")
    private Integer idOpreme;

    @Column(name = "naziv_opreme", nullable = false, length = 255)
    private String nazivOpreme;

    @Column(name = "cijena", precision = 10, scale = 2)
    private BigDecimal cijena;

    @Enumerated(EnumType.STRING)
    @Column(name = "kategorija", nullable = false)
    private KategorijaOpreme kategorija;

    @Column(name = "slika")
    private String slika;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vlasnik_opreme", nullable = false)
    private IzvodacKorisnik vlasnikOpreme;

    @OneToMany(mappedBy = "oprema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpremaLokacije> lokacije = new ArrayList<>();
}