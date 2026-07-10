package javarax.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Публікується після успішного зняття коштів з рахунку (AccountService.withdraw).
 */
public record MoneyWithdrawnEvent(
        Long transactionId,
        Long accountId,
        BigDecimal amount,
        BigDecimal newBalance,
        Instant occurredAt
) {
}
