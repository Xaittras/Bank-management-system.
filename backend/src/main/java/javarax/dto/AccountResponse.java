package javarax.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountResponse {
	private Long  id;
	private String accountNumber;
	private BigDecimal balance;
}