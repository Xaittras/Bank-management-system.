package javarax.controller;

import java.time.Duration;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
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

	private static final int MAX_ATTEMPTS = 5;
	private static final Duration LOCK_WINDOW = Duration.ofMinutes(5);

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final UserService userService;
	private final RedissonClient redissonClient;

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
		String email = request.getEmail().trim().toLowerCase();
		RAtomicLong attempts = redissonClient.getAtomicLong("login_attempts:" + email);

		if (attempts.get() >= MAX_ATTEMPTS) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.body("Забагато невдалих спроб. Спробуйте через 5 хвилин");
		}

		try {
			Authentication auth = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
			attempts.delete();
			UserDetails user = (UserDetails) auth.getPrincipal();
			return ResponseEntity.ok(new AuthResponse(jwtService.generateToken(user)));
		} catch (AuthenticationException e) {
			// Рахуємо лише справжні невдалі автентифікації, а не будь-які винятки (збій БД/Redis тощо)
			attempts.incrementAndGet();
			// TTL ставимо при кожній невдачі: якщо процес упаде між increment і expire,
			// лічильник без TTL заблокував би користувача назавжди
			attempts.expire(LOCK_WINDOW);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невірний email або пароль");
		}
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
		// Помилки (дублікат email -> 409, валідація -> 400) обробляє GlobalExceptionHandler
		userService.register(request);
		UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());
		return ResponseEntity.ok(new AuthResponse(jwtService.generateToken(user)));
	}
}
