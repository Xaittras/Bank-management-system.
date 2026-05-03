package javarax.dto;

import lombok.Data;

@Data
public class UserDto {

	private Long id;
	private String email;
	private String name;
	private String role;

	public UserDto(Long id, String email, String name, String role) {
		this.id = id;
		this.email = email;
		this.name = name;
		this.role = role;

	}
}
