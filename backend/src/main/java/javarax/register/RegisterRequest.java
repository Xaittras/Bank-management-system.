package javarax.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

	// 72 байти це межа BCrypt
	@NotBlank
	@Size(min = 8, max = 72, message = "пароль має містити від 8 до 72 символів")
	private String password;

	@NotBlank
	@Size(max = 100)
	private String name;

	@NotBlank
	@Email
	@Size(max = 254)
	private String email;
}
