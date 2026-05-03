package javarax.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javarax.dto.UserDto;
import javarax.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final UserService userService;

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/users")
	public List<UserDto> getAllUsers() {
		return userService.getAllUsers();
	}
}
