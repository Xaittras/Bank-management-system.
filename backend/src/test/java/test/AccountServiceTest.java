package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javarax.dto.AccountResponse;
import javarax.exception.ResourceNotFoundException;
import javarax.kafka.EventPublisher;
import javarax.model.Account;
import javarax.model.Transaction;
import javarax.model.User;
import javarax.service.AccountService;
import javarax.storage.AccountRepository;
import javarax.storage.TransactionRepository;
import javarax.storage.UserRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private EventPublisher eventPublisher;

	@Mock
	private RLock lock;

	@InjectMocks
	private AccountService accountService;

	private User user;
	private Account account;

	@BeforeEach
	void setUp() {

		user = new User();
		user.setId(1L);
		user.setEmail("test@gmail.com");

		account = new Account();
		account.setId(1L);
		account.setUser(user);
		account.setBalance(BigDecimal.valueOf(100));
	}

	/**
	 * deposit()/withdraw() беруть distributed lock через Redisson перед тим,
	 * як торкнутись балансу. У тесті мокаємо lock так, ніби він одразу вільний.
	 */
	private void stubLockAcquired() throws InterruptedException {
		when(redissonClient.getLock(anyString())).thenReturn(lock);
		when(lock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
		when(lock.isHeldByCurrentThread()).thenReturn(true);
	}

	@Test
	void shouldCreateAccount() {

		when(userRepository.findById(1L))
				.thenReturn(Optional.of(user));

		when(accountRepository.save(any(Account.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Account created = accountService.createAccount(1L, "Main Account");

		assertNotNull(created);
		assertEquals("Main Account", created.getName());
		assertEquals(BigDecimal.ZERO, created.getBalance());

		verify(accountRepository).save(any(Account.class));
	}

	@Test
	void shouldDepositMoney() throws InterruptedException {

		stubLockAcquired();

		when(accountRepository.findByIdAndUser_Id(1L, 1L))
				.thenReturn(Optional.of(account));
		when(transactionRepository.save(any(Transaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		accountService.deposit(1L, 1L, BigDecimal.valueOf(50));

		assertEquals(
				BigDecimal.valueOf(150),
				account.getBalance()
				);

		verify(transactionRepository).save(any());
	}

	@Test
	void shouldWithdrawMoney() throws InterruptedException {

		stubLockAcquired();

		when(accountRepository.findByIdAndUser_Id(1L, 1L))
				.thenReturn(Optional.of(account));
		when(transactionRepository.save(any(Transaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		accountService.withdraw(1L, 1L, BigDecimal.valueOf(40));

		assertEquals(
				BigDecimal.valueOf(60),
				account.getBalance()
				);

		verify(transactionRepository).save(any());
	}

	@Test
	void shouldThrowExceptionWhenBalanceInsufficient() throws InterruptedException {

		stubLockAcquired();

		when(accountRepository.findByIdAndUser_Id(1L, 1L))
				.thenReturn(Optional.of(account));

		IllegalArgumentException ex = assertThrows(
				IllegalArgumentException.class,
				() -> accountService.withdraw(
						1L,
						1L,
						BigDecimal.valueOf(1000)
						)
				);

		assertEquals("Insufficient balance", ex.getMessage());
	}

	@Test
	void shouldThrowExceptionForNegativeDeposit() {

		// validateAmount() кидає виняток ДО того, як бере lock,
		// тож стаб на redissonClient тут не потрібен
		IllegalArgumentException ex = assertThrows(
				IllegalArgumentException.class,
				() -> accountService.deposit(
						1L,
						1L,
						BigDecimal.valueOf(-10)
						)
				);

		assertEquals("Amount must be positive", ex.getMessage());
	}

	@Test
	void shouldReturnAccountsByEmail() {

		when(userRepository.findByEmail("test@gmail.com"))
				.thenReturn(Optional.of(user));

		when(accountRepository.findAllByUserId(1L))
				.thenReturn(List.of(account));

		List<AccountResponse> result =
				accountService.getAccountsByEmail("test@gmail.com");

		assertEquals(1, result.size());
		assertEquals(account.getId(), result.get(0).getId());
	}

	@Test
	void shouldThrowWhenUserNotFound() {

		when(userRepository.findByEmail("wrong@gmail.com"))
				.thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> accountService.getAccountsByEmail("wrong@gmail.com")
				);
	}

}
