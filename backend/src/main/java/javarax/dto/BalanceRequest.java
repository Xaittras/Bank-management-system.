package javarax.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceRequest {

	@NotNull
	@Positive
	@Digits(integer = 15, fraction = 2, message = "максимум 2 знаки після коми")
	@DecimalMax("1000000000000")
	private BigDecimal amount;

	@NotNull
	private Long accountId;
}
