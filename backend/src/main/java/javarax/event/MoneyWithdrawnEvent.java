package javarax.event;

import java.math.BigDecimal;
import java.time.Instant;


public record MoneyWithdrawnEvent(
		Long transactionId,
		Long accountId,
		BigDecimal amount,
		BigDecimal newBalance,
		Instant occurredAt
		) {
}
