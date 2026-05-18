package hr.beatsync.backend.dto;

import hr.beatsync.backend.model.Recenzija;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecenzijaAdminResponse {

    private Integer idRezervacije;
    private BigDecimal ocjena;
    private String komentar;
    private LocalDateTime datumRecenzije;
    private String statusValidacije;

    private String usernameIzvodac;
    private String izvodacIme;
    private String izvodacPrezime;
    private String usernameBusiness;
    private String nazivKluba;

    public static RecenzijaAdminResponse from(Recenzija r) {
        RecenzijaAdminResponse dto = new RecenzijaAdminResponse();
        dto.setIdRezervacije(r.getIdRezervacije());
        dto.setOcjena(r.getOcjena());
        dto.setKomentar(r.getKomentar());
        dto.setDatumRecenzije(r.getDatumRecenzije());
        dto.setStatusValidacije(r.getStatusValidacije().name());

        if (r.getRezervacija() != null) {
            if (r.getRezervacija().getIzvodacRezervacija() != null) {
                dto.setUsernameIzvodac(r.getRezervacija().getIzvodacRezervacija().getUsernameIzvodac());
                dto.setIzvodacIme(r.getRezervacija().getIzvodacRezervacija().getIme());
                dto.setIzvodacPrezime(r.getRezervacija().getIzvodacRezervacija().getPrezime());
            }
            if (r.getRezervacija().getBusinessRezervacija() != null) {
                dto.setUsernameBusiness(r.getRezervacija().getBusinessRezervacija().getUsernameBusiness());
                dto.setNazivKluba(r.getRezervacija().getBusinessRezervacija().getNazivKluba());
            }
        }
        return dto;
    }
}
