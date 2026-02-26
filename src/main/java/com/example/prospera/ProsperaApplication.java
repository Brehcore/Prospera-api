package com.example.prospera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan("com.example.prospera")
@EnableJpaRepositories("com.example.prospera")
@EnableAsync
public class ProsperaApplication {

	public static void main(String[] args) {
        SpringApplication.run(ProsperaApplication.class, args);
	}

}
