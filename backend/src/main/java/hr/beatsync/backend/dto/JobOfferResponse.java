package hr.beatsync.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class JobOfferResponse {
    private Integer idPonude;
    private String nazivPonude;
    private LocalDate datum;
    private LocalTime pocetak;
    private LocalTime kraj;
    private String lokacija;
    private BigDecimal budzet;
    private String opisPosla;
    private String potrebnoIskustvo;
    private String businessUsername;
    private String nazivKluba;
    private boolean jeliPrijavljen;
    private int brojPrijava;
}
