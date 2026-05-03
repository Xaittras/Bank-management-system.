package javarax.config;




import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javarax.dto.CustomUserDetails;
import javarax.dto.Role;
import javarax.model.User;
import javarax.storage.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Service
@Primary
@RequiredArgsConstructor

public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override

	public UserDetails loadUserByUsername(String email) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		String role = user.getRole() != null
				? user.getRole().name()
						: "USER";

		return new CustomUserDetails(
				user,
				List.of(new SimpleGrantedAuthority("ROLE_" + role))
				);

	}
	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository,
			PasswordEncoder passwordEncoder) {

		return args -> {

			if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

				User admin = new User();
				admin.setEmail("admin@gmail.com");
				admin.setName("Admin");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole(Role.ADMIN);
				admin.setAccounts(new ArrayList<>());

				userRepository.save(admin);
			}
		};

	}

}















