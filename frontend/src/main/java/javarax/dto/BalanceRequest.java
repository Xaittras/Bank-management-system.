package javarax.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalanceRequest {
	private BigDecimal amount;
	private Long accountId;


}