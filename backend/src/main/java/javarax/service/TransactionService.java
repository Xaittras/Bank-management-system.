package javarax.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import javarax.dto.TransactionDto;
import javarax.dto.TransactionResponseDto;
import javarax.model.Account;
import javarax.model.Transaction;
import javarax.storage.AccountRepository;
import javarax.storage.TransactionRepository;

@Service

public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final AccountRepository accountRepository;

	public TransactionService(TransactionRepository transactionRepository,
			AccountRepository accountRepository) {
		this.transactionRepository = transactionRepository;
		this.accountRepository = accountRepository;
	}

	public TransactionResponseDto create(TransactionDto dto) {

		Account account = accountRepository.findById(dto.getAccountId())
				.orElseThrow(() -> new RuntimeException("Account not found"));

		Transaction transaction = new Transaction();
		transaction.setAmount(dto.getAmount());
		transaction.setType(dto.getType());
		transaction.setDescription(dto.getDescription());
		transaction.setCreatedAt(LocalDateTime.now());
		transaction.setAccount(account);

		Transaction saved = transactionRepository.save(transaction);

		return mapToDto(saved);
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
	public List<TransactionResponseDto> getByAccountId(Long accountId) {
		List<Transaction> list = transactionRepository.findByAccountId(accountId);

		return list.stream()
				.map(this::mapToDto)
				.toList();
	}
	public Transaction getById(Long id) {
		return transactionRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Transaction not found"));
	}
}