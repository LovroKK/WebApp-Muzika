package hr.beatsync.backend.dto;

import hr.beatsync.backend.model.Recenzija;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecenzijaResponse {

    private Integer idRezervacije;
    private BigDecimal ocjena;
    private String komentar;
    private LocalDateTime datumRecenzije;

    private String businessUsername;
    private String nazivKluba;

    public static RecenzijaResponse from(Recenzija r) {
        RecenzijaResponse dto = new RecenzijaResponse();
        dto.setIdRezervacije(r.getIdRezervacije());
        dto.setOcjena(r.getOcjena());
        dto.setKomentar(r.getKomentar());
        dto.setDatumRecenzije(r.getDatumRecenzije());

        if (r.getRezervacija() != null && r.getRezervacija().getBusinessRezervacija() != null) {
            dto.setBusinessUsername(r.getRezervacija().getBusinessRezervacija().getUsernameBusiness());
            dto.setNazivKluba(r.getRezervacija().getBusinessRezervacija().getNazivKluba());
        }

        return dto;
    }
}
