package javarax.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

// @Data на сутності з зв'язком небезпечний (equals/hashCode/toString тягнуть lazy-зв'язки), тому лише Getter/Setter
@Getter
@Setter
@Entity
@Table(name = "transactions", indexes = @Index(name = "idx_tx_account_created", columnList = "account_id, createdAt"))
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Гроші не можна зберігати в Double: помилки округлення
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	private String type; // DEPOSIT, WITHDRAW

	private String description;

	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
}
