package javarax.event;

import java.time.Instant;

/**
 * Публікується після створення нового банківського рахунку (AccountService.createAccount).
 */
public record AccountCreatedEvent(
        Long accountId,
        Long userId,
        String accountNumber,
        Instant createdAt
) {
}
