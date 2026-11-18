package javarax.idempotency;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.Supplier;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;

/**
 * Ідемпотентне виконання операції: запис ключа та бізнес-операція комітяться в ОДНІЙ транзакції.
 * - успіх   -> ключ і результат збережені разом;
 * - помилка -> відкат усього разом із ключем, повтор безпечний;
 * - повтор  -> повертається збережений результат, операція не виконується вдруге;
 * - гонка   -> друга вставка чекає коміту першої на унікальному індексі, а потім читає її результат.
 */
@Service
@RequiredArgsConstructor
public class IdempotencyExecutor {

	public static final String REPLAY_HEADER = "Idempotent-Replayed";

	private final IdempotencyKeyRepository repository;
	private final TransactionTemplate tx;

	public ResponseEntity<String> execute(Long userId, String key, String requestHash,
			Supplier<BigDecimal> operation) {
		try {
			return tx.execute(status -> run(userId, key, requestHash, operation));
		} catch (DataIntegrityViolationException e) {
			// Конкурентний запит з тим самим ключем уже закомітився; наша транзакція відкотилась
			return tx.execute(status -> repository.findByUserIdAndKey(userId, key)
					.map(rec -> replay(rec, requestHash))
					.orElseThrow(() -> e));
		}
	}

	private ResponseEntity<String> run(Long userId, String key, String requestHash,
			Supplier<BigDecimal> operation) {
		var existing = repository.findByUserIdAndKey(userId, key);
		if (existing.isPresent()) {
			return replay(existing.get(), requestHash);
		}

		IdempotencyKey rec = new IdempotencyKey();
		rec.setUserId(userId);
		rec.setKey(key);
		rec.setRequestHash(requestHash);
		rec.setResponseStatus(HttpStatus.OK.value());
		repository.saveAndFlush(rec); // резервуємо ключ; при гонці тут чекаємо/падаємо на унікальному індексі

		BigDecimal balance = operation.get(); // будь-який виняток -> відкат разом із ключем

		rec.setResponseBody(balance.toPlainString());
		repository.save(rec);
		return json(rec.getResponseStatus(), rec.getResponseBody(), false);
	}

	private ResponseEntity<String> replay(IdempotencyKey rec, String requestHash) {
		if (!rec.getRequestHash().equals(requestHash)) {
			return ResponseEntity.unprocessableEntity().body("Idempotency-Key уже використано з іншим запитом");
		}
		return json(rec.getResponseStatus(), rec.getResponseBody(), true);
	}

	private ResponseEntity<String> json(int status, String body, boolean replayed) {
		ResponseEntity.BodyBuilder b = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON);
		if (replayed) {
			b.header(REPLAY_HEADER, "true");
		}
		return b.body(body);
	}

	public static String hash(String operation, Long accountId, BigDecimal amount) {
		try {
			String raw = operation + ":" + accountId + ":" + amount.stripTrailingZeros().toPlainString();
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
