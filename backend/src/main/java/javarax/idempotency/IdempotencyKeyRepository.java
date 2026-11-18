package javarax.idempotency;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

	Optional<IdempotencyKey> findByUserIdAndKey(Long userId, String key);

	@Modifying
	@Transactional
	@Query("delete from IdempotencyKey k where k.createdAt < :threshold")
	int deleteOlderThan(@Param("threshold") Instant threshold);
}
