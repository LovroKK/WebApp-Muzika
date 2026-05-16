package hr.beatsync.backend.dto;

import hr.beatsync.backend.model.Poruka;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PorukaResponse {
    private Integer idPoruke;
    private String sadrzajPoruke;
    private LocalDateTime timestampPoruke;
    private String posiljatelj;
    private Boolean readStatus;
    private String messageType;
    private Integer idRezervacije;

    public static PorukaResponse from(Poruka p) {
        PorukaResponse dto = new PorukaResponse();
        dto.setIdPoruke(p.getIdPoruke());
        dto.setSadrzajPoruke(p.getSadrzajPoruke());
        dto.setTimestampPoruke(p.getTimestampPoruke());
        dto.setPosiljatelj(p.getPosiljatelj().name());
        dto.setReadStatus(p.getReadStatus());
        dto.setMessageType(p.getMessageType());
        dto.setIdRezervacije(p.getIdRezervacije());
        return dto;
    }
}
