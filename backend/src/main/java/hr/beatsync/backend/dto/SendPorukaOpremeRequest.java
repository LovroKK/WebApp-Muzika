package hr.beatsync.backend.dto;

import lombok.Data;

@Data
public class SendPorukaOpremeRequest {
    private Integer najamId;
    private String sadrzajPoruke;
}
