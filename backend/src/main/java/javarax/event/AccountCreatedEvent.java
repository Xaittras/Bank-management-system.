package javarax.event;

import java.time.Instant;


public record AccountCreatedEvent(
		Long accountId,
		Long userId,
		String accountNumber,
		Instant createdAt
		) {
}
