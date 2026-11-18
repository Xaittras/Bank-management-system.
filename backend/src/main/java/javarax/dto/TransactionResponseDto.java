package javarax.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
	private Long id;
	private BigDecimal amount;
	private String type;
	private String description;
	private LocalDateTime createdAt;
	private Long accountId;
}
