package javarax.event;

import java.math.BigDecimal;
import java.time.Instant;


public record TransactionCreatedEvent(
		Long transactionId,
		Long accountId,
		String type,
		BigDecimal amount,
		Instant occurredAt
		) {
}
