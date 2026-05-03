package javarax.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getServletPath().startsWith("/api/auth/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain chain) throws ServletException, IOException {

		try {
			String authHeader = request.getHeader("Authorization");

			if (authHeader != null && authHeader.startsWith("Bearer ")) {

				String token = authHeader.substring(7);
				String username = jwtService.extractUsername(token);

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

					UserDetails userDetails = userDetailsService.loadUserByUsername(username);

					if (jwtService.isTokenValid(token, userDetails)) {

						UsernamePasswordAuthenticationToken auth =
								new UsernamePasswordAuthenticationToken(
										userDetails,
										null,
										userDetails.getAuthorities()
										);

						SecurityContextHolder.getContext().setAuthentication(auth);
					}
				}
			}

		} catch (JwtException e) {
			System.out.println("Invalid JWT: " + e.getMessage());
		}

		// 🔥 ВАЖЛИВО — завжди викликається
		chain.doFilter(request, response);
	}
}




