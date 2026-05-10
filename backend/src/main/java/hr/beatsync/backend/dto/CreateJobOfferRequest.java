package hr.beatsync.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateJobOfferRequest {
    private String nazivPonude;
    private LocalDate datum;
    private LocalTime pocetak;
    private LocalTime kraj;
    private String lokacija;
    private BigDecimal budzet;
    private String zanrovi;
    private String opremaZahtjevi;
    private String ostaloOprema;
    private String opisPosla;
    private String potrebnoIskustvo;
}
