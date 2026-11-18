package javarax.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAccountRequest {
	@Size(max = 100)
	private String name;
}
