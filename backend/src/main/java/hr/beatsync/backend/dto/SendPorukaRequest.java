package hr.beatsync.backend.dto;

import lombok.Data;

@Data
public class SendPorukaRequest {
    private String sadrzajPoruke;
    private Integer idRezervacije;
}
