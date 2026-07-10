package javarax.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javarax.dto.Role;
import javarax.dto.UserDto;
import javarax.dto.UserMapper;
import javarax.event.UserCreatedEvent;
import javarax.kafka.EventPublisher;
import javarax.kafka.KafkaTopics;
import javarax.model.User;
import javarax.register.RegisterRequest;
import javarax.storage.UserRepository;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class UserService {

	private	final AccountService accountService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final EventPublisher eventPublisher;

	public User register(RegisterRequest request) {

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email вже використовується");
		}

		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.USER);

		User savedUser = userRepository.save(user);

		eventPublisher.publish(KafkaTopics.USER_EVENTS, savedUser.getId().toString(),
				new UserCreatedEvent(savedUser.getId(), savedUser.getEmail(), Instant.now()));

		// 🔥 ОЦЕ ВАЖЛИВО
		accountService.createAccount(savedUser.getId(), "Main account");

		return savedUser;
	}

	public List<UserDto> getAllUsers() {
		return userRepository.findAll()
				.stream()
				.map(userMapper::toDto)
				.toList();
	}
}