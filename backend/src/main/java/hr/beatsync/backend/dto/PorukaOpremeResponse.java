package hr.beatsync.backend.dto;

import hr.beatsync.backend.model.PorukaOpreme;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PorukaOpremeResponse {

    private Integer idPorukeOpreme;
    private Integer najamId;
    private String sadrzajPoruke;
    private LocalDateTime timestampPoruke;
    private String posiljateljeUsername;
    private Boolean readStatus;
    private String messageType;

    public static PorukaOpremeResponse from(PorukaOpreme p) {
        PorukaOpremeResponse dto = new PorukaOpremeResponse();
        dto.setIdPorukeOpreme(p.getIdPorukeOpreme());
        dto.setNajamId(p.getNajam().getIdNajma());
        dto.setSadrzajPoruke(p.getSadrzajPoruke());
        dto.setTimestampPoruke(p.getTimestampPoruke());
        dto.setPosiljateljeUsername(p.getPosiljateljeUsername());
        dto.setReadStatus(p.getReadStatus());
        dto.setMessageType(p.getMessageType());
        return dto;
    }
}
