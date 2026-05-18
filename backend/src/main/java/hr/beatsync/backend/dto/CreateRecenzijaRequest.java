package hr.beatsync.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRecenzijaRequest {

    @NotNull
    private Integer idRezervacije;

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("5.0")
    private BigDecimal ocjena;

    @Size(max = 1000)
    private String komentar;
}
