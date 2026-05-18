package hr.beatsync.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeatsyncBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeatsyncBackendApplication.class, args);
	}

}
