package com.example.docgen.config;

import com.example.docgen.entities.User;
import com.example.docgen.entities.enums.UserRole;
import com.example.docgen.repositories.UserRepository;
import com.example.docgen.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@Profile("test")
public class TestConfig {

	@Autowired
	UserRepository userRepository;
	@Autowired
	PasswordEncoder passwordEncoder;

	@Bean
	CommandLineRunner run(UserService userService) {
		return args -> {

			if (userRepository.findByEmail("admin@admin").isEmpty()) {
				User user = new User();
				user.setName("Administrador");
				user.setEmail("admin@admin");
				user.setPassword(passwordEncoder.encode("271114")); // senha criptografada
				user.setCpf("12345678900"); // CPF válido
				user.setPhone("81989674212");
				user.setBirthDate(LocalDate.of(1990, 01, 01));
				user.setRole(UserRole.ADMIN);

				userRepository.save(user);
				System.out.println("Usuário admin criado com sucesso.");

			}

		};

	}

}
