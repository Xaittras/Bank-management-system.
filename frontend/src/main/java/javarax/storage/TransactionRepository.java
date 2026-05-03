package javarax.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import javarax.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findAllByAccountIdOrderByCreatedAtDesc(Long accountId);
	List<Transaction> findByAccountId(Long accountId);
}