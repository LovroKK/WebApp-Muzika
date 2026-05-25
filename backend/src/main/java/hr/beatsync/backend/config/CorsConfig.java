package hr.beatsync.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // allowedOriginPatterns (NE allowedOrigins) jer koristimo wildcard u portu - allowedOrigins ne podrzava "*" u portu.
                        // Pokriva Live Server (5500), Docker frontend (8081) i sve lokalne portove,
                        // privatne IP raspone lokalne mreze za demo s drugih uredjaja (mobitel/laptop na istom WiFi).
                        .allowedOriginPatterns(
                                "http://localhost:*", "http://127.0.0.1:*",
                                "http://192.168.*:*",   
                                "http://10.*:*",        
                                "http://172.*:*"        
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}