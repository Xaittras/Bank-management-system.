
package javarax.dto;

import java.util.List;

public class UserAccountsResponse {

	private Long userId;
	private List<AccountResponse> accounts;

	public UserAccountsResponse(Long userId, List<AccountResponse> accounts) {
		this.userId = userId;
		this.accounts = accounts;
	}

	public Long getUserId() {
		return userId;
	}

	public List<AccountResponse> getAccounts() {
		return accounts;
	}
}

