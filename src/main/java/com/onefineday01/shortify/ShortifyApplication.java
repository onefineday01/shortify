package com.onefineday01.shortify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ShortifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShortifyApplication.class, args);
	}

}
