package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import javarax.storage.UserRepository;
import javarax.service.UserService;
import javarax.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        User savedUser = new User();
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

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowException_whenEmailAlreadyExists() {
        // given
        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(new User()));

        // when + then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        assertEquals("Email вже використовується", exception.getMessage());

        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}