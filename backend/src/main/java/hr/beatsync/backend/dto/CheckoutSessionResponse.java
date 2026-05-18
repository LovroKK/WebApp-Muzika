package hr.beatsync.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutSessionResponse {
    private String checkoutUrl;
    private String sessionId;
}
