package hr.beatsync.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterBusinessRequest {

    @NotBlank(message = "Username je obavezan")
    @Size(min = 3, max = 50)
    private String usernameBusiness;

    @NotBlank(message = "Naziv kluba je obavezan")
    private String nazivKluba;

    @NotBlank(message = "Email je obavezan")
    @Email
    private String email;

    @NotBlank(message = "Lozinka je obavezna")
    @Size(min = 6, message = "Lozinka mora imati barem 6 znakova")
    private String lozinka;

    @NotBlank(message = "Lokacija je obavezna")
    private String lokacija;

    @NotBlank(message = "Opis je obavezan")
    private String opis;

    @NotBlank(message = "Broj telefona je obavezan")
    private String brojTelefona;
}
