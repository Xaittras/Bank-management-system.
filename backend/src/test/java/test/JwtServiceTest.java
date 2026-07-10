package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import javarax.config.JwtService;

class JwtServiceTest {

	private JwtService jwtService;

	private User userDetails;

	@BeforeEach
	void setUp() {

		jwtService = new JwtService();

		userDetails = new User(
				"test@gmail.com",
				"password",
				List.of(() -> "ROLE_USER")
				);
	}

	@Test
	void shouldGenerateToken() {

		String token = jwtService.generateToken(userDetails);

		assertNotNull(token);
		assertFalse(token.isBlank());
	}

	@Test
	void shouldExtractUsername() {

		String token = jwtService.generateToken(userDetails);

		String username = jwtService.extractUsername(token);

		assertEquals("test@gmail.com", username);
	}

	@Test
	void shouldExtractRoles() {

		String token = jwtService.generateToken(userDetails);

		List<String> roles = jwtService.extractRoles(token);

		assertEquals(1, roles.size());
		assertEquals("ROLE_USER", roles.get(0));
	}

	@Test
	void shouldValidateToken() {

		String token = jwtService.generateToken(userDetails);

		boolean valid = jwtService.isTokenValid(token, userDetails);

		assertTrue(valid);
	}

	@Test
	void shouldRejectTokenForDifferentUser() {

		String token = jwtService.generateToken(userDetails);

		User anotherUser = new User(
				"admin@gmail.com",
				"password",
				List.of(() -> "ROLE_ADMIN")
				);

		boolean valid = jwtService.isTokenValid(token, anotherUser);

		assertFalse(valid);
	}
}
