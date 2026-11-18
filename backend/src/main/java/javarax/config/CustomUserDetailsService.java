package javarax.config;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javarax.dto.CustomUserDetails;
import javarax.model.User;
import javarax.storage.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) {
		User user = userRepository.findByEmail(email == null ? null : email.trim().toLowerCase())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		String role = user.getRole() != null ? user.getRole().name() : "USER";
		return new CustomUserDetails(user, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
	}
}
