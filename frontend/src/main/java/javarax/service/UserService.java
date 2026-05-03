package javarax.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javarax.dto.Role;
import javarax.dto.UserDto;
import javarax.dto.UserMapper;
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

	public User register(RegisterRequest request) {




		User user = new User();
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.USER);

		User savedUser = userRepository.save(user);

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