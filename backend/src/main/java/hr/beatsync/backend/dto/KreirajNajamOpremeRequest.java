package hr.beatsync.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class KreirajNajamOpremeRequest {
    private Integer opremaId;
    private LocalDate periodOd;
    private LocalDate periodDo;
    private String napomena;
}
