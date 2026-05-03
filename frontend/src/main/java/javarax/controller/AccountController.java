

package javarax.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javarax.dto.AccountResponse;
import javarax.dto.BalanceRequest;
import javarax.dto.CreateAccountRequest;
import javarax.dto.CustomUserDetails;
import javarax.dto.TransactionResponseDto;
import javarax.model.Account;
import javarax.service.AccountService;
import javarax.service.TransactionService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;
	private final TransactionService transactionService;


	@GetMapping("/accounts")
	public List<AccountResponse> getAccounts(Authentication auth) {
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		return accountService.getAccounts(user.getId());
	}
	@PostMapping("/accounts")
	public AccountResponse createAccount(
			@RequestBody CreateAccountRequest request,
			Authentication auth) {

		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

		Account account = accountService.createAccount(
				user.getId(),
				request.getName()
				);

		return new AccountResponse(
				account.getId(),
				account.getAccountNumber(),
				account.getBalance()
				);
	}

	@RestControllerAdvice
	public class GlobalExceptionHandler {

		@ExceptionHandler(IllegalArgumentException.class)
		public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
			return ResponseEntity
					.status(HttpStatus.CONFLICT)
					.body(e.getMessage());
		}
	}
	@PostMapping("/deposit")
	public ResponseEntity<?> deposit(@RequestBody BalanceRequest request,
			Authentication auth) {

		CustomUserDetails user =
				(CustomUserDetails) auth.getPrincipal();

		Account account = accountService.deposit(
				user.getId(),
				request.getAccountId(),
				request.getAmount()
				);

		return ResponseEntity.ok(account.getBalance());
	}
	@PostMapping("/withdraw")
	public ResponseEntity<?> withdraw(@RequestBody BalanceRequest request,
			Authentication auth) {

		CustomUserDetails user =
				(CustomUserDetails) auth.getPrincipal();

		Account account = accountService.withdraw(
				user.getId(),
				request.getAccountId(),
				request.getAmount()
				);

		return ResponseEntity.ok(account.getBalance());
	}
	@GetMapping("/accounts/{accountId}/transactions")
	public List<TransactionResponseDto> getByAccountId(@PathVariable Long accountId) {
		return transactionService.getByAccountId(accountId);
	}



} 
























