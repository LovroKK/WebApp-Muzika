package hr.beatsync.backend.dto;

import hr.beatsync.backend.enums.KategorijaOpreme;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateEquipmentRequest {

    @NotBlank
    private String nazivOpreme;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal cijena;

    @NotNull
    private KategorijaOpreme kategorija;

    private String slika;

    @NotEmpty
    private List<@NotBlank String> lokacije;
}
