package javarax.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import javarax.dto.AccountResponse;
import javarax.dto.UserAccountsResponse;
import javarax.event.AccountCreatedEvent;
import javarax.event.MoneyDepositedEvent;
import javarax.event.MoneyWithdrawnEvent;
import javarax.event.TransactionCreatedEvent;
import javarax.exception.ResourceNotFoundException;
import javarax.kafka.EventPublisher;
import javarax.kafka.KafkaTopics;
import javarax.model.Account;
import javarax.model.Transaction;
import javarax.model.User;
import javarax.storage.AccountRepository;
import javarax.storage.TransactionRepository;
import javarax.storage.UserRepository;

@Service
public class AccountService {

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;
	private final RedissonClient redissonClient;
	private final EventPublisher eventPublisher;

	public AccountService(AccountRepository accountRepository, UserRepository userRepository,
			TransactionRepository transactionRepository, RedissonClient redissonClient,
			EventPublisher eventPublisher) {
		this.accountRepository = accountRepository;
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
		this.redissonClient = redissonClient;
		this.eventPublisher = eventPublisher;
	}

	public Account createAccount(Long userId, String name) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Account account = new Account();
		account.setUser(user);
		account.setBalance(BigDecimal.ZERO);
		account.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8));

		if (name != null && !name.isBlank()) {
			account.setName(name);
		} else {
			account.setName("My Account");
		}

		Account saved = accountRepository.save(account);

		eventPublisher.publish(KafkaTopics.ACCOUNT_EVENTS, saved.getId().toString(),
				new AccountCreatedEvent(saved.getId(), userId, saved.getAccountNumber(), Instant.now()));

		return saved;
	}

	private AccountResponse mapToDto(Account account) {
		return new AccountResponse(
				account.getId(),
				account.getAccountNumber(),
				account.getBalance()
				);
	}

	public List<AccountResponse> getAccounts(Long userId) {
		return accountRepository.findAllByUserId(userId)
				.stream()
				.map(this::mapToDto)
				.toList();
	}

	private Transaction createTransaction(Account account, BigDecimal amount, String type, String desc) {
		Transaction tx = new Transaction();
		tx.setAccount(account);
		tx.setAmount(amount.doubleValue());
		tx.setType(type);
		tx.setDescription(desc);
		tx.setCreatedAt(LocalDateTime.now());
		return transactionRepository.save(tx);
	}

	/**
	 * Перевіряє idempotency-key. Повертає true, якщо ключ новий (можна продовжувати),
	 * false — якщо запит з таким ключем вже обробляється/оброблений.
	 */
	public boolean acquireIdempotencyKey(String key) {
		return redissonClient.getBucket("idem:" + key)
				.setIfAbsent("processing", Duration.ofMinutes(5));
	}

	public void releaseIdempotencyKeyOnError(String key) {
		redissonClient.getBucket("idem:" + key).delete();
	}

	@Transactional
	public Account deposit(Long userId, Long accountId, BigDecimal amount) {
		validateAmount(amount);

		RLock lock = redissonClient.getLock("account_lock:" + accountId);
		boolean locked = false;
		try {
			locked = lock.tryLock(5, 3, TimeUnit.SECONDS);
			if (!locked) {
				throw new IllegalStateException("Рахунок зараз зайнятий іншою операцією, спробуйте ще раз");
			}

			Account account = accountRepository
					.findByIdAndUser_Id(accountId, userId)
					.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

			account.setBalance(account.getBalance().add(amount));
			Transaction tx = createTransaction(account, amount, "DEPOSIT", "Deposit");

			eventPublisher.publish(KafkaTopics.TRANSACTION_EVENTS, accountId.toString(),
					new MoneyDepositedEvent(tx.getId(), accountId, amount, account.getBalance(), Instant.now()));
			eventPublisher.publish(KafkaTopics.TRANSACTION_EVENTS, accountId.toString(),
					new TransactionCreatedEvent(tx.getId(), accountId, "DEPOSIT", amount, Instant.now()));

			return account;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Lock interrupted", e);
		} finally {
			if (locked && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	@Transactional
	public Account withdraw(Long userId, Long accountId, BigDecimal amount) {
		validateAmount(amount);

		RLock lock = redissonClient.getLock("account_lock:" + accountId);
		boolean locked = false;
		try {
			locked = lock.tryLock(5, 3, TimeUnit.SECONDS);
			if (!locked) {
				throw new IllegalStateException("Рахунок зараз зайнятий іншою операцією, спробуйте ще раз");
			}

			Account account = accountRepository
					.findByIdAndUser_Id(accountId, userId)
					.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

			if (account.getBalance().compareTo(amount) < 0) {
				throw new IllegalArgumentException("Insufficient balance");
			}

			account.setBalance(account.getBalance().subtract(amount));
			Transaction tx = createTransaction(account, amount, "WITHDRAW", "Withdraw");

			eventPublisher.publish(KafkaTopics.TRANSACTION_EVENTS, accountId.toString(),
					new MoneyWithdrawnEvent(tx.getId(), accountId, amount, account.getBalance(), Instant.now()));
			eventPublisher.publish(KafkaTopics.TRANSACTION_EVENTS, accountId.toString(),
					new TransactionCreatedEvent(tx.getId(), accountId, "WITHDRAW", amount, Instant.now()));

			return account;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Lock interrupted", e);
		} finally {
			if (locked && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private void validateAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be positive");
		}
	}

	public UserAccountsResponse getUserAccounts(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<AccountResponse> accounts = accountRepository.findAllByUserId(user.getId())
				.stream()
				.map(this::mapToDto)
				.toList();

		return new UserAccountsResponse(user.getId(), accounts);
	}

	public List<AccountResponse> getAccountsByEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		return accountRepository.findAllByUserId(user.getId())
				.stream()
				.map(this::mapToDto)
				.toList();
	}

	public List<Transaction> getAccountHistory(Long accountId) {
		return transactionRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId);
	}
}