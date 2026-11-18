package javarax.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000000");

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;
	private final EventPublisher eventPublisher;

	public AccountService(AccountRepository accountRepository, UserRepository userRepository,
			TransactionRepository transactionRepository, EventPublisher eventPublisher) {
		this.accountRepository = accountRepository;
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public Account createAccount(Long userId, String name) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Account account = new Account();
		account.setUser(user);
		account.setBalance(BigDecimal.ZERO);
		// 16 hex-символів замість 8: 8 символів давали помітну ймовірність колізії з unique-обмеженням
		account.setAccountNumber("ACC" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
		account.setName(name != null && !name.isBlank() ? name.trim() : "My Account");

		Account saved = accountRepository.save(account);

		eventPublisher.publish(KafkaTopics.ACCOUNT_EVENTS, saved.getId().toString(),
				new AccountCreatedEvent(saved.getId(), userId, saved.getAccountNumber(), Instant.now()));
		return saved;
	}

	private AccountResponse mapToDto(Account account) {
		return new AccountResponse(account.getId(), account.getAccountNumber(), account.getBalance());
	}

	@Transactional(readOnly = true)
	public List<AccountResponse> getAccounts(Long userId) {
		return accountRepository.findAllByUserId(userId).stream().map(this::mapToDto).toList();
	}

	private Transaction createTransaction(Account account, BigDecimal amount, String type, String desc) {
		Transaction tx = new Transaction();
		tx.setAccount(account);
		tx.setAmount(amount);
		tx.setType(type);
		tx.setDescription(desc);
		tx.setCreatedAt(LocalDateTime.now());
		return transactionRepository.save(tx);
	}

	/**
	 * Має виконуватись в одній транзакції з IdempotencyExecutor (propagation REQUIRED, за замовчуванням).
	 * Блокування рядка рахунку (FOR UPDATE) тримається до коміту.
	 */
	@Transactional
	public Account deposit(Long userId, Long accountId, BigDecimal amount) {
		validateAmount(amount);

		Account account = accountRepository.findForUpdate(accountId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		account.setBalance(account.getBalance().add(amount));
		Transaction tx = createTransaction(account, amount, "DEPOSIT", "Deposit");

		eventPublisher.publish(KafkaTopics.TRANSACTION_EVENTS, accountId.toString(),
				new MoneyDepositedEvent(tx.getId(), accountId, amount, account.getBalance(), Instant.now()));
		eventPublisher.publish(KafkaTopics.TRANSACTION_EVENTS, accountId.toString(),
				new TransactionCreatedEvent(tx.getId(), accountId, "DEPOSIT", amount, Instant.now()));
		return account;
	}

	@Transactional
	public Account withdraw(Long userId, Long accountId, BigDecimal amount) {
		validateAmount(amount);

		Account account = accountRepository.findForUpdate(accountId, userId)
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
	}

	private void validateAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be positive");
		}
		if (amount.stripTrailingZeros().scale() > 2) {
			throw new IllegalArgumentException("Amount must have at most 2 decimal places");
		}
		if (amount.compareTo(MAX_AMOUNT) > 0) {
			throw new IllegalArgumentException("Amount is too large");
		}
	}

	@Transactional(readOnly = true)
	public UserAccountsResponse getUserAccounts(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		List<AccountResponse> accounts = accountRepository.findAllByUserId(user.getId())
				.stream().map(this::mapToDto).toList();
		return new UserAccountsResponse(user.getId(), accounts);
	}

	@Transactional(readOnly = true)
	public List<AccountResponse> getAccountsByEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		return accountRepository.findAllByUserId(user.getId()).stream().map(this::mapToDto).toList();
	}
}
