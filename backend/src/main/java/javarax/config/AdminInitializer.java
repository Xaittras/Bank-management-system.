package javarax.config;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import javarax.dto.Role;
import javarax.model.User;
import javarax.storage.UserRepository;

/** Створює початкового адміна лише якщо пароль заданий через оточення (без захардкоджених облікових даних). */
@Configuration
public class AdminInitializer {

	private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder,
			@Value("${app.admin.email}") String email,
			@Value("${app.admin.password:}") String password) {
		return args -> {
			if (password == null || password.isBlank()) {
				log.info("ADMIN_PASSWORD не задано: початковий адмін не створюється");
				return;
			}
			String normalized = email.trim().toLowerCase();
			if (userRepository.findByEmail(normalized).isEmpty()) {
				User admin = new User();
				admin.setEmail(normalized);
				admin.setName("Admin");
				admin.setPassword(passwordEncoder.encode(password));
				admin.setRole(Role.ADMIN);
				admin.setAccounts(new ArrayList<>());
				userRepository.save(admin);
				log.info("Створено початкового адміна {}", normalized);
			}
		};
	}
}
