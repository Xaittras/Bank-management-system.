package javarax.register;


import lombok.Data;
@Data
public class RegisterRequest {

	private String password;
	private String name;
	private String email;


}