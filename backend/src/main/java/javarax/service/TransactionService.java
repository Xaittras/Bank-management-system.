package javarax.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javarax.dto.TransactionResponseDto;
import javarax.exception.ResourceNotFoundException;
import javarax.model.Transaction;
import javarax.storage.AccountRepository;
import javarax.storage.TransactionRepository;

@Service
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final AccountRepository accountRepository;

	public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
		this.transactionRepository = transactionRepository;
		this.accountRepository = accountRepository;
	}

	/** Повертає транзакції лише якщо рахунок належить користувачу (захист від IDOR). */
	@Transactional(readOnly = true)
	public List<TransactionResponseDto> getByAccountId(Long userId, Long accountId) {
		if (!accountRepository.existsByIdAndUserId(accountId, userId)) {
			// 404, а не 403: не розкриваємо факт існування чужого рахунку
			throw new ResourceNotFoundException("Account not found");
		}
		return transactionRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId)
				.stream().map(this::mapToDto).toList();
	}

	private TransactionResponseDto mapToDto(Transaction t) {
		TransactionResponseDto dto = new TransactionResponseDto();
		dto.setId(t.getId());
		dto.setAmount(t.getAmount());
		dto.setType(t.getType());
		dto.setDescription(t.getDescription());
		dto.setCreatedAt(t.getCreatedAt());
		dto.setAccountId(t.getAccount().getId());
		return dto;
	}
}
