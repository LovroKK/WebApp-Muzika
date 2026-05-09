package hr.beatsync.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


////////////////////////////////////////////////////////////////////////////
//  
// !!VAZNO JOS PROVJERITI!!
//
////////////////////////////////////////////////////////////////////////////

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username je obavezan")
    private String username;

    @NotBlank(message = "Lozinka je obavezna")
    private String password;
}