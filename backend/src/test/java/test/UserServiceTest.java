package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javarax.kafka.EventPublisher;
import javarax.model.User;
import javarax.register.RegisterRequest;
import javarax.service.AccountService;
import javarax.service.UserService;
import javarax.storage.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private AccountService accountService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private UserService userService;

	private RegisterRequest request;

	@BeforeEach
	void setUp() {
		request = new RegisterRequest();
		request.setName("John");
		request.setEmail("john@example.com");
		request.setPassword("123456");
	}

	@Test
	void shouldRegisterUser_whenEmailIsUnique() {
		// given
		when(userRepository.existsByEmail(request.getEmail()))
				.thenReturn(false);

		when(passwordEncoder.encode(request.getPassword()))
				.thenReturn("encodedPassword");

		User savedUser = new User();
		savedUser.setId(1L);
		savedUser.setName("John");
		savedUser.setEmail("john@example.com");
		savedUser.setPassword("encodedPassword");

		when(userRepository.save(any(User.class)))
				.thenReturn(savedUser);

		// when
		User result = userService.register(request);

		// then
		assertNotNull(result);
		assertEquals("John", result.getName());
		assertEquals("john@example.com", result.getEmail());
		assertEquals("encodedPassword", result.getPassword());

		verify(userRepository).existsByEmail(request.getEmail());
		verify(passwordEncoder).encode(request.getPassword());
		verify(userRepository).save(any(User.class));
		verify(accountService).createAccount(1L, "Main account");
	}

	@Test
	void shouldThrowException_whenEmailAlreadyExists() {
		// given
		when(userRepository.existsByEmail(request.getEmail()))
				.thenReturn(true);

		// when + then
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> userService.register(request)
		);

		assertEquals("Email вже використовується", exception.getMessage());

		verify(userRepository).existsByEmail(request.getEmail());
		verify(userRepository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}
}
