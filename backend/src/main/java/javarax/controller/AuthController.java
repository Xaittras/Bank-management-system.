package javarax.controller;

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

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthRequest request) {

		System.out.println("LOGIN HIT");

		Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword()
						)
				);

		UserDetails user = (UserDetails) auth.getPrincipal();

		String token = jwtService.generateToken(user);
		System.out.println("EMAIL: " + request.getEmail());
		System.out.println("PASS: " + request.getPassword());
		return ResponseEntity.ok(new AuthResponse(token));
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		try {
			userService.register(request);

			UserDetails user =
					userDetailsService.loadUserByUsername(request.getEmail());

			String token = jwtService.generateToken(user);

			return ResponseEntity.ok(new AuthResponse(token));

		} catch (Exception e) {
			e.printStackTrace();

			if (e.getMessage().contains("duplicate") || 
					e.getMessage().contains("unique")) {
				return ResponseEntity
						.status(400)
						.body("User already exists");
			}

			return ResponseEntity
					.status(500)
					.body("Something went wrong");

		}
	}
}





