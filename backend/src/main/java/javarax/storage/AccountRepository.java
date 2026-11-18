package javarax.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import javarax.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

	List<Account> findAllByUserId(Long userId);

	Optional<Account> findByIdAndUser_Id(Long accountId, Long userId);

	boolean existsByIdAndUserId(Long accountId, Long userId);

	/**
	 * SELECT ... FOR UPDATE: рядок блокується до кінця транзакції (до коміту), тому
	 * конкурентні зняття/поповнення виконуються послідовно. Redisson-лок знімався ДО коміту,
	 * тож між unlock і commit інший потік міг прочитати старий баланс.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Account a where a.id = :accountId and a.user.id = :userId")
	Optional<Account> findForUpdate(@Param("accountId") Long accountId, @Param("userId") Long userId);
}
