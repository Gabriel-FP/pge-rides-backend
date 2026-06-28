package br.gov.pge.rides;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RidesBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RidesBackendApplication.class, args);
	}

}
