package hr.beatsync.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SlikeProstoraResponse {
    private Integer idSlike;
    private String opisSlike;
    private String url;
}
