package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
	private EventPublisher eventPublisher;

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

	@Test
	void shouldCreateAccount() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(accountRepository.save(any(Account.class))).thenAnswer(i -> {
			Account a = i.getArgument(0);
			a.setId(10L);
			return a;
		});

		Account created = accountService.createAccount(1L, "Main Account");

		assertNotNull(created);
		assertEquals("Main Account", created.getName());
		assertEquals(BigDecimal.ZERO, created.getBalance());
		assertEquals(19, created.getAccountNumber().length()); // "ACC" + 16
		verify(accountRepository).save(any(Account.class));
	}

	@Test
	void shouldDepositMoney() {
		when(accountRepository.findForUpdate(1L, 1L)).thenReturn(Optional.of(account));
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

		accountService.deposit(1L, 1L, BigDecimal.valueOf(50));

		assertEquals(0, BigDecimal.valueOf(150).compareTo(account.getBalance()));
		verify(transactionRepository).save(any());
	}

	@Test
	void shouldWithdrawMoney() {
		when(accountRepository.findForUpdate(1L, 1L)).thenReturn(Optional.of(account));
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

		accountService.withdraw(1L, 1L, BigDecimal.valueOf(40));

		assertEquals(0, BigDecimal.valueOf(60).compareTo(account.getBalance()));
		verify(transactionRepository).save(any());
	}

	@Test
	void shouldThrowExceptionWhenBalanceInsufficient() {
		when(accountRepository.findForUpdate(1L, 1L)).thenReturn(Optional.of(account));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> accountService.withdraw(1L, 1L, BigDecimal.valueOf(1000)));

		assertEquals("Insufficient balance", ex.getMessage());
		assertEquals(0, BigDecimal.valueOf(100).compareTo(account.getBalance()));
		verify(transactionRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any(), any(), any());
	}

	@Test
	void shouldThrowExceptionForNegativeDeposit() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> accountService.deposit(1L, 1L, BigDecimal.valueOf(-10)));
		assertEquals("Amount must be positive", ex.getMessage());
	}

	@Test
	void shouldRejectAmountWithMoreThanTwoDecimals() {
		assertThrows(IllegalArgumentException.class,
				() -> accountService.deposit(1L, 1L, new BigDecimal("10.001")));
	}

	@Test
	void shouldNotFindForeignAccount() {
		when(accountRepository.findForUpdate(1L, 2L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> accountService.deposit(2L, 1L, BigDecimal.TEN));
	}

	@Test
	void shouldReturnAccountsByEmail() {
		when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
		when(accountRepository.findAllByUserId(1L)).thenReturn(List.of(account));

		List<AccountResponse> result = accountService.getAccountsByEmail("test@gmail.com");

		assertEquals(1, result.size());
		assertEquals(account.getId(), result.get(0).getId());
	}

	@Test
	void shouldThrowWhenUserNotFound() {
		when(userRepository.findByEmail("wrong@gmail.com")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> accountService.getAccountsByEmail("wrong@gmail.com"));
	}
}
