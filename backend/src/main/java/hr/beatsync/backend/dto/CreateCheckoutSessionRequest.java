package hr.beatsync.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCheckoutSessionRequest {

    @NotNull
    private Integer idRezervacije;
}
