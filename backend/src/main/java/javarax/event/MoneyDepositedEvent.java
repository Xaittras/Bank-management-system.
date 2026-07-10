package javarax.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Публікується після успішного поповнення рахунку (AccountService.deposit).
 * Містить деталі, специфічні саме для депозиту (напр. для тексту notification-повідомлення).
 */
public record MoneyDepositedEvent(
        Long transactionId,
        Long accountId,
        BigDecimal amount,
        BigDecimal newBalance,
        Instant occurredAt
) {
}
