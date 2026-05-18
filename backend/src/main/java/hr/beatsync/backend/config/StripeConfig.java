package hr.beatsync.backend.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    private static final Logger log = LoggerFactory.getLogger(StripeConfig.class);

    @Value("${stripe.secret.key:}")
    private String apiKey;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("stripe.secret.key NIJE postavljen — endpointi za plaćanje će vraćati grešku dok ne postaviš ključ u application.properties");
            return;
        }
        Stripe.apiKey = apiKey;
        log.info("Stripe SDK inicijaliziran (mode: {})", apiKey.startsWith("sk_test_") ? "TEST" : "LIVE");
    }
}
