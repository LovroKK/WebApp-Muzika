package hr.beatsync.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

////////////////////////////////////////////////////////////////////////////
//  
// !!VAZNO JOS PROVJERITI!!
//
////////////////////////////////////////////////////////////////////////////

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

    private String brojTelefona;

    @NotBlank(message = "Lozinka je obavezna")
    @Size(min = 6, message = "Lozinka mora imati barem 6 znakova")
    private String lozinka;

    private String linkMixtape;
    private BigDecimal cijenaPoSatu;
    private String kratkiOpis;
    private String prijasnjiPoslovi;
    private Integer radiOd;
    private Integer ukupnoGodinaIskustva;
}