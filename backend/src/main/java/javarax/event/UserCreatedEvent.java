package javarax.event;

import java.time.Instant;

/**
 * Публікується одразу після успішної реєстрації користувача (UserService.register).
 */
public record UserCreatedEvent(
        Long userId,
        String email,
        Instant createdAt
) {
}
