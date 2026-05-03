

package javarax.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import javarax.dto.AccountResponse;
import javarax.dto.UserAccountsResponse;
import javarax.exception.ResourceNotFoundException;
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

	public AccountService(AccountRepository accountRepository, UserRepository userRepository, TransactionRepository transactionRepository ) {
		this.accountRepository = accountRepository;
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
	}

	public Account createAccount(Long userId, String name) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Account account = new Account();
		account.setUser(user);
		account.setBalance(BigDecimal.ZERO);
		account.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8));

		// 👉 optional
		if (name != null && !name.isBlank()) {
			account.setName(name);
		} else {
			account.setName("My Account"); // дефолт
		}

		return accountRepository.save(account);
	}

	private AccountResponse mapToDto(Account account) {
		return new AccountResponse(
				account.getId(),
				account.getAccountNumber(), // ⚠️ also fixed (see below)
				account.getBalance()
				);
	}
	public List<AccountResponse> getAccounts(Long userId) {
		return accountRepository.findAllByUserId(userId)
				.stream()
				.map(this::mapToDto)
				.toList();

	}
	private void createTransaction(Account account, BigDecimal amount, String type, String desc) {
		Transaction tx = new Transaction();
		tx.setAccount(account);
		tx.setAmount(amount.doubleValue());
		tx.setType(type);
		tx.setDescription(desc);
		tx.setCreatedAt(LocalDateTime.now());

		transactionRepository.save(tx);
	}

	@Transactional
	public Account deposit(Long userId, Long accountId, BigDecimal amount) {

		validateAmount(amount);

		Account account = accountRepository
				.findByIdAndUser_Id(accountId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		account.setBalance(account.getBalance().add(amount));

		createTransaction(account, amount, "DEPOSIT", "Deposit");


		return account;
	}

	@Transactional
	public Account withdraw(Long userId, Long accountId, BigDecimal amount) {

		validateAmount(amount);

		Account account = accountRepository
				.findByIdAndUser_Id(accountId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));

		if (account.getBalance().compareTo(amount) < 0) {
			throw new IllegalArgumentException("Insufficient balance");
		}

		account.setBalance(account.getBalance().subtract(amount));

		createTransaction(account, amount, "WITHDRAW", "Withdraw");
		return account;
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

