package hr.beatsync.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterIzvodacRequest {

    @NotBlank(message = "Username je obavezan")
    @Size(min = 3, max = 50)
    private String usernameIzvodac;

    @NotBlank(message = "Ime je obavezno")
    private String ime;

    @NotBlank(message = "Prezime je obavezno")
    private String prezime;

    @NotBlank(message = "Email je obavezan")
    @Email
    private String email;

    @NotBlank(message = "Broj telefona je obavezan")
    private String brojTelefona;

    @NotBlank(message = "Lozinka je obavezna")
    @Size(min = 6, message = "Lozinka mora imati barem 6 znakova")
    private String lozinka;

    @NotBlank(message = "Link na mixtape je obavezan")
    private String linkMixtape;

    @NotNull(message = "Cijena po satu je obavezna")
    private BigDecimal cijenaPoSatu;

    @NotBlank(message = "Kratki opis je obavezan")
    private String kratkiOpis;

    @NotBlank(message = "Prijašnji poslovi su obavezni")
    private String prijasnjiPoslovi;

    @NotNull(message = "Godina početka rada je obavezna")
    private Integer radiOd;

    @NotNull(message = "Ukupno godina iskustva je obavezno")
    private Integer ukupnoGodinaIskustva;
}
