package javarax.idempotency;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** Видаляє ключі старші за 24 години, щоб таблиця не росла безмежно. */
@Component
@RequiredArgsConstructor
public class IdempotencyCleanupJob {

	private static final Logger log = LoggerFactory.getLogger(IdempotencyCleanupJob.class);
	private static final Duration TTL = Duration.ofHours(24);

	private final IdempotencyKeyRepository repository;

	@Scheduled(fixedDelay = 3_600_000, initialDelay = 60_000)
	@Transactional
	public void cleanup() {
		int removed = repository.deleteOlderThan(Instant.now().minus(TTL));
		if (removed > 0) {
			log.info("Видалено {} прострочених idempotency-ключів", removed);
		}
	}
}
