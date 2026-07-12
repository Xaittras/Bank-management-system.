package javarax.event;

import java.time.Instant;


public record UserCreatedEvent(
		Long userId,
		String email,
		Instant createdAt
		) {
}
