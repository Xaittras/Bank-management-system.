package javarax.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import javarax.dto.AccountResponse;
import javarax.dto.BalanceRequest;
import javarax.dto.CreateAccountRequest;
import javarax.dto.CustomUserDetails;
import javarax.dto.TransactionResponseDto;
import javarax.idempotency.IdempotencyExecutor;
import javarax.model.Account;
import javarax.service.AccountService;
import javarax.service.TransactionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AccountController {

	private static final int MAX_KEY_LENGTH = 128;

	private final AccountService accountService;
	private final TransactionService transactionService;
	private final IdempotencyExecutor idempotency;

	@FunctionalInterface
	private interface BalanceOperation {
		Account apply(Long userId, Long accountId, BigDecimal amount);
	}

	@GetMapping("/accounts")
	public List<AccountResponse> getAccounts(Authentication auth) {
		return accountService.getAccounts(userId(auth));
	}

	@PostMapping("/accounts")
	public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request, Authentication auth) {
		Account account = accountService.createAccount(userId(auth), request.getName());
		return new AccountResponse(account.getId(), account.getAccountNumber(), account.getBalance());
	}

	@PostMapping("/deposit")
	public ResponseEntity<String> deposit(@Valid @RequestBody BalanceRequest request,
			@RequestHeader("Idempotency-Key") String idemKey, Authentication auth) {
		return executeIdempotent("deposit", request, idemKey, auth, accountService::deposit);
	}

	@PostMapping("/withdraw")
	public ResponseEntity<String> withdraw(@Valid @RequestBody BalanceRequest request,
			@RequestHeader("Idempotency-Key") String idemKey, Authentication auth) {
		return executeIdempotent("withdraw", request, idemKey, auth, accountService::withdraw);
	}

	@GetMapping("/accounts/{accountId}/transactions")
	public List<TransactionResponseDto> getByAccountId(@PathVariable Long accountId, Authentication auth) {
		return transactionService.getByAccountId(userId(auth), accountId);
	}

	private ResponseEntity<String> executeIdempotent(String operation, BalanceRequest request, String idemKey,
			Authentication auth, BalanceOperation action) {
		if (idemKey == null || idemKey.isBlank() || idemKey.length() > MAX_KEY_LENGTH) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Idempotency-Key має бути непорожнім і не довшим за " + MAX_KEY_LENGTH + " символів");
		}
		Long userId = userId(auth);
		String hash = IdempotencyExecutor.hash(operation, request.getAccountId(), request.getAmount());
		return idempotency.execute(userId, idemKey.trim(), hash,
				() -> action.apply(userId, request.getAccountId(), request.getAmount()).getBalance());
	}

	private Long userId(Authentication auth) {
		return ((CustomUserDetails) auth.getPrincipal()).getId();
	}
}
