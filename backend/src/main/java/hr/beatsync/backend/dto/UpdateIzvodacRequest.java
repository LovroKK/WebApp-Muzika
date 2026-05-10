package hr.beatsync.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateIzvodacRequest {

    @NotBlank(message = "Ime je obavezno")
    private String ime;

    @NotBlank(message = "Prezime je obavezno")
    private String prezime;

    @NotBlank(message = "Email je obavezan")
    @Email
    private String email;

    @NotBlank(message = "Broj telefona je obavezan")
    private String brojTelefona;

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
