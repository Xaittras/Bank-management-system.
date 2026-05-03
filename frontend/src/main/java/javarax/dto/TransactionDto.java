package javarax.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionDto {
	private Double amount;
	private String type;
	private String description;
	private Long accountId;
}