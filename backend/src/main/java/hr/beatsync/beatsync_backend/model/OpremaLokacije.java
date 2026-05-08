package hr.beatsync.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oprema_lokacije")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpremaLokacije {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opreme", nullable = false)
    private Oprema oprema;

    @Column(name = "lokacija", nullable = false, length = 255)
    private String lokacija;
}