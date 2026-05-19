package hr.beatsync.backend.dto;

import hr.beatsync.backend.model.NajamOpreme;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class NajamOpremeResponse {

    private Integer idNajma;
    private Integer opremaId;
    private String nazivOpreme;
    private String kategorijaOpreme;
    private BigDecimal cijenaOpreme;
    private String vlasnikUsername;
    private String najmoprimacUsername;
    private String najmoprimacTip;
    private LocalDate periodOd;
    private LocalDate periodDo;
    private String status;
    private LocalDateTime timestampZahtjeva;
    private String napomena;

    public static NajamOpremeResponse from(NajamOpreme n) {
        NajamOpremeResponse dto = new NajamOpremeResponse();
        dto.setIdNajma(n.getIdNajma());
        dto.setOpremaId(n.getOprema().getIdOpreme());
        dto.setNazivOpreme(n.getOprema().getNazivOpreme());
        dto.setKategorijaOpreme(n.getOprema().getKategorija().name());
        dto.setCijenaOpreme(n.getOprema().getCijena());
        dto.setVlasnikUsername(n.getVlasnikUsername());
        dto.setNajmoprimacUsername(n.getNajmoprimacUsername());
        dto.setNajmoprimacTip(n.getNajmoprimacTip().name());
        dto.setPeriodOd(n.getPeriodOd());
        dto.setPeriodDo(n.getPeriodDo());
        dto.setStatus(n.getStatus().name());
        dto.setTimestampZahtjeva(n.getTimestampZahtjeva());
        dto.setNapomena(n.getNapomena());
        return dto;
    }
}
