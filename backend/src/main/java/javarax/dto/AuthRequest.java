package javarax.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequest {
	@NotBlank
	@Size(max = 254)
	private String email;

	@NotBlank
	@Size(max = 72)
	private String password;
}
