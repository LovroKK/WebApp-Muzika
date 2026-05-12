package hr.beatsync.backend.dto;

import hr.beatsync.backend.enums.KategorijaOpreme;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class EquipmentResponse {
    private Integer idOpreme;
    private String nazivOpreme;
    private BigDecimal cijena;
    private KategorijaOpreme kategorija;
    private String slika;
    private String vlasnikUsername;
    private List<String> lokacije;
}
