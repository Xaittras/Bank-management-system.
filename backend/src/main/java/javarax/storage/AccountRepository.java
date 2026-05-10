package javarax.storage;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import javarax.model.Account;


public interface AccountRepository extends JpaRepository<Account, Long> {



	List<Account> findAllByUserId(Long userId);


	Optional<Account> findByIdAndUser_Id(Long accountId, Long id);




}