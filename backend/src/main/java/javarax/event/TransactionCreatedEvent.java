package javarax.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Загальна подія "відбулась транзакція" — публікується для будь-якого типу операції
 * (DEPOSIT / WITHDRAW / TRANSFER). Призначена для consumer'ів, яким не важливий тип
 * операції, а важливий сам факт + сума (наприклад audit/compliance-логування).
 */
public record TransactionCreatedEvent(
        Long transactionId,
        Long accountId,
        String type,
        BigDecimal amount,
        Instant occurredAt
) {
}
