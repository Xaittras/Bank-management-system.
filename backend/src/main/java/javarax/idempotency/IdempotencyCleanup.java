package javarax.idempotency;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Видаляє ключі старші за 24 години (вікно, в якому клієнт може безпечно повторювати запит). */
@Component
@RequiredArgsConstructor
public class IdempotencyCleanup {

	private static final Logger log = LoggerFactory.getLogger(IdempotencyCleanup.class);
	private final IdempotencyKeyRepository repository;

	@Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT5M")
	public void cleanup() {
		int deleted = repository.deleteOlderThan(Instant.now().minus(Duration.ofHours(24)));
		if (deleted > 0) {
			log.info("Видалено {} прострочених ключів ідемпотентності", deleted);
		}
	}
}
