package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javarax.idempotency.IdempotencyExecutor;
import javarax.idempotency.IdempotencyKey;
import javarax.idempotency.IdempotencyKeyRepository;

@ExtendWith(MockitoExtension.class)
class IdempotencyExecutorTest {

	@Mock
	private IdempotencyKeyRepository repository;
	@Mock
	private TransactionTemplate tx;

	private IdempotencyExecutor executor;

	@BeforeEach
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void setUp() {
		// TransactionTemplate просто виконує колбек у тестах
		lenient().when(tx.execute(any())).thenAnswer(i -> ((TransactionCallback) i.getArgument(0)).doInTransaction(null));
		executor = new IdempotencyExecutor(repository, tx);
	}

	@Test
	void firstRequestExecutesOperationAndStoresResult() {
		when(repository.findByUserIdAndKey(1L, "k1")).thenReturn(Optional.empty());
		String hash = IdempotencyExecutor.hash("deposit", 5L, BigDecimal.TEN);

		ResponseEntity<String> resp = executor.execute(1L, "k1", hash, () -> new BigDecimal("110.00"));

		assertEquals(HttpStatus.OK, resp.getStatusCode());
		assertEquals("110.00", resp.getBody());
		verify(repository).saveAndFlush(any(IdempotencyKey.class));
	}

	@Test
	void repeatedRequestReturnsStoredResultWithoutRunningOperation() {
		String hash = IdempotencyExecutor.hash("deposit", 5L, BigDecimal.TEN);
		IdempotencyKey stored = new IdempotencyKey();
		stored.setRequestHash(hash);
		stored.setResponseStatus(200);
		stored.setResponseBody("110.00");
		when(repository.findByUserIdAndKey(1L, "k1")).thenReturn(Optional.of(stored));

		AtomicInteger calls = new AtomicInteger();
		ResponseEntity<String> resp = executor.execute(1L, "k1", hash, () -> {
			calls.incrementAndGet();
			return BigDecimal.ZERO;
		});

		assertEquals(HttpStatus.OK, resp.getStatusCode());
		assertEquals("110.00", resp.getBody());
		assertEquals(0, calls.get());
		assertEquals("true", resp.getHeaders().getFirst(IdempotencyExecutor.REPLAY_HEADER));
		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void sameKeyWithDifferentRequestIsRejected() {
		IdempotencyKey stored = new IdempotencyKey();
		stored.setRequestHash(IdempotencyExecutor.hash("deposit", 5L, BigDecimal.TEN));
		stored.setResponseStatus(200);
		stored.setResponseBody("110.00");
		when(repository.findByUserIdAndKey(1L, "k1")).thenReturn(Optional.of(stored));

		String otherHash = IdempotencyExecutor.hash("deposit", 5L, BigDecimal.valueOf(999));
		ResponseEntity<String> resp = executor.execute(1L, "k1", otherHash, () -> BigDecimal.ZERO);

		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resp.getStatusCode());
	}

	@Test
	void failedOperationPropagatesAndStoresNothingFinal() {
		when(repository.findByUserIdAndKey(1L, "k1")).thenReturn(Optional.empty());
		String hash = IdempotencyExecutor.hash("withdraw", 5L, BigDecimal.TEN);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> executor.execute(1L, "k1", hash, () -> {
					throw new IllegalArgumentException("Insufficient balance");
				}));

		assertTrue(ex.getMessage().contains("Insufficient"));
		// у реальній транзакції виняток відкочує і запис ключа
		verify(repository, never()).save(any());
	}

	@Test
	void hashIgnoresTrailingZerosButNotOperationOrAmount() {
		assertEquals(IdempotencyExecutor.hash("deposit", 1L, new BigDecimal("10.00")),
				IdempotencyExecutor.hash("deposit", 1L, new BigDecimal("10")));
		assertTrue(!IdempotencyExecutor.hash("deposit", 1L, BigDecimal.TEN)
				.equals(IdempotencyExecutor.hash("withdraw", 1L, BigDecimal.TEN)));
	}
}
