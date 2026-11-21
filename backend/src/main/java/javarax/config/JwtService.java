package javarax.config;

import java.security.Key;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javarax.dto.CustomUserDetails;

@Service

public class JwtService {

    private final Key signKey;
    private final long expirationMs;
public  JwtService(
        @Value("${JWT_SECRET}") String secret,
        @Value("${JWT_EXPIRATION_MS:3600000}") long expirationMs) {

    byte[] keyBytes = Decoders.BASE64.decode(secret);

    if (keyBytes.length < 32) {
        throw new IllegalStateException(
            "JWT_SECRET має містити щонайменше 32 байти (base64)"
        );
    }

    this.signKey = Keys.hmacShaKeyFor(keyBytes);
    this.expirationMs = expirationMs;
}

	public String extractUsername(String token) {
		return extractClaims(token).getSubject();
	}

	@SuppressWarnings("unchecked")
	public List<String> extractRoles(String token) {
		Object roles = extractClaims(token).get("role");
		return roles == null ? List.of() : (List<String>) roles;
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		Claims claims = extractClaims(token);
		return claims.getSubject().equals(userDetails.getUsername())
				&& !claims.getExpiration().before(new Date());
	}

	public String generateToken(UserDetails userDetails) {
		JwtBuilder builder = Jwts.builder()
				.setSubject(userDetails.getUsername())
				.claim("role", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expirationMs));
		if (userDetails instanceof CustomUserDetails cud) {
			builder.claim("userId", cud.getId());
		}
		return builder.signWith(signKey, SignatureAlgorithm.HS256).compact();
	}

	private Claims extractClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(signKey).build().parseClaimsJws(token).getBody();
	}
}
