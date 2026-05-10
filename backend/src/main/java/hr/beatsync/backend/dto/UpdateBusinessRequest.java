package hr.beatsync.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateBusinessRequest {

    @NotBlank(message = "Naziv kluba je obavezan")
    private String nazivKluba;

    @NotBlank(message = "Lokacija je obavezna")
    private String lokacija;

    @NotBlank(message = "Opis je obavezan")
    private String opis;

    @NotBlank(message = "Email je obavezan")
    @Email
    private String email;

    @NotBlank(message = "Broj telefona je obavezan")
    private String brojTelefona;

    private String residentDjUsername;
    private String najboljiIzvodaci;
}
