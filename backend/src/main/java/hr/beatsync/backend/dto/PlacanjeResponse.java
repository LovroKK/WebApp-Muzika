package hr.beatsync.backend.dto;

import hr.beatsync.backend.model.Placanje;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PlacanjeResponse {

    private Integer idPlacanja;
    private Integer idRezervacije;
    private BigDecimal iznos;
    private String currency;
    private String statusPlacanja;
    private LocalDateTime datumKreiranja;
    private LocalDateTime datumPlacanja;

    public static PlacanjeResponse from(Placanje p) {
        PlacanjeResponse dto = new PlacanjeResponse();
        dto.setIdPlacanja(p.getIdPlacanja());
        if (p.getRezervacija() != null) {
            dto.setIdRezervacije(p.getRezervacija().getIdRezervacije());
        }
        dto.setIznos(p.getIznos());
        dto.setCurrency(p.getCurrency());
        dto.setStatusPlacanja(p.getStatusPlacanja().name());
        dto.setDatumKreiranja(p.getDatumKreiranja());
        dto.setDatumPlacanja(p.getDatumPlacanja());
        return dto;
    }
}
