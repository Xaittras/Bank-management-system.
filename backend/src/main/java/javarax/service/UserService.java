package javarax.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javarax.dto.Role;
import javarax.dto.UserDto;
import javarax.dto.UserMapper;
import javarax.event.UserCreatedEvent;
import javarax.exception.ConflictException;
import javarax.kafka.EventPublisher;
import javarax.kafka.KafkaTopics;
import javarax.model.User;
import javarax.register.RegisterRequest;
import javarax.storage.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final AccountService accountService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final EventPublisher eventPublisher;

	/** Одна транзакція: користувач і його перший рахунок створюються разом або не створюються взагалі. */
	@Transactional
	public User register(RegisterRequest request) {
		String email = request.getEmail().trim().toLowerCase();

		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email вже використовується");
		}

		User user = new User();
		user.setName(request.getName().trim());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.USER);

		User savedUser = userRepository.save(user);

		accountService.createAccount(savedUser.getId(), "Main account");

		eventPublisher.publish(KafkaTopics.USER_EVENTS, savedUser.getId().toString(),
				new UserCreatedEvent(savedUser.getId(), savedUser.getEmail(), Instant.now()));

		return savedUser;
	}

	@Transactional(readOnly = true)
	public List<UserDto> getAllUsers() {
		return userRepository.findAll().stream().map(userMapper::toDto).toList();
	}
}
