package hr.beatsync.backend.dto;

import hr.beatsync.backend.model.Rezervacija;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RezervacijaResponse {

    private Integer idRezervacije;
    private LocalDateTime periodOd;
    private LocalDateTime periodDo;
    private String statusRezervacije;
    private Boolean potvrdaRezervacije;
    private Boolean potvrdaIzvodac;
    private Boolean potvrdaBusiness;

    private String izvodacUsername;
    private String izvodacIme;
    private String izvodacPrezime;

    private String businessUsername;
    private String nazivKluba;

    private String nazivPonude;
    private LocalDate datumPonude;
    private BigDecimal budzet;

    public static RezervacijaResponse from(Rezervacija r) {
        RezervacijaResponse dto = new RezervacijaResponse();
        dto.setIdRezervacije(r.getIdRezervacije());
        dto.setPeriodOd(r.getPeriodOd());
        dto.setPeriodDo(r.getPeriodDo());
        dto.setStatusRezervacije(r.getStatusRezervacije().name());
        dto.setPotvrdaRezervacije(r.getPotvrdaRezervacije());
        dto.setPotvrdaIzvodac(r.getPotvrdaIzvodac());
        dto.setPotvrdaBusiness(r.getPotvrdaBusiness());

        dto.setIzvodacUsername(r.getIzvodacRezervacija().getUsernameIzvodac());
        dto.setIzvodacIme(r.getIzvodacRezervacija().getIme());
        dto.setIzvodacPrezime(r.getIzvodacRezervacija().getPrezime());

        dto.setBusinessUsername(r.getBusinessRezervacija().getUsernameBusiness());
        dto.setNazivKluba(r.getBusinessRezervacija().getNazivKluba());

        if (r.getJobOffer() != null) {
            dto.setNazivPonude(r.getJobOffer().getNazivPonude());
            dto.setDatumPonude(r.getJobOffer().getDatum());
            dto.setBudzet(r.getJobOffer().getBudzet());
        }

        return dto;
    }
}
