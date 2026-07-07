package javarax.controller;

import java.time.Duration;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javarax.config.JwtService;
import javarax.dto.AuthRequest;
import javarax.dto.AuthResponse;
import javarax.register.RegisterRequest;
import javarax.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final UserService userService;
	private final RedissonClient redissonClient;

	private static final int MAX_ATTEMPTS = 5;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthRequest request) {
		String attemptsKey = "login_attempts:" + request.getEmail();
		RAtomicLong attempts = redissonClient.getAtomicLong(attemptsKey);

		long current = attempts.get();
		if (current >= MAX_ATTEMPTS) {
			return ResponseEntity
					.status(HttpStatus.TOO_MANY_REQUESTS)
					.body("Забагато невдалих спроб. Спробуйте через 5 хвилин");
		}

		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
					);
			attempts.delete(); // успішний логін — скидаємо лічильник
			UserDetails user = (UserDetails) auth.getPrincipal();
			String token = jwtService.generateToken(user);
			return ResponseEntity.ok(new AuthResponse(token));

		} catch (Exception e) {
			long newCount = attempts.incrementAndGet();
			if (newCount == 1) {
				attempts.expire(Duration.ofMinutes(5));
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невірний email або пароль");
		}
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		try {
			userService.register(request);
			UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());
			String token = jwtService.generateToken(user);
			return ResponseEntity.ok(new AuthResponse(token));
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && (e.getMessage().contains("duplicate") || e.getMessage().contains("unique"))) {
				return ResponseEntity.status(400).body("User already exists");
			}
			return ResponseEntity.status(500).body("Something went wrong");
		}
	}
}