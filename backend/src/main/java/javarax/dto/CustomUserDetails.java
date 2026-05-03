package javarax.dto;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javarax.model.User;

public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L; // 🔥 fix warning

	private Long id;
	private String email;
	private String password;

	private Collection<? extends GrantedAuthority> authorities;

	public CustomUserDetails(User user,
			Collection<? extends GrantedAuthority> authorities) {

		this.id = user.getId();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.authorities = authorities;
	}

	// 🔥 CUSTOM FIELD
	public Long getId() {
		return id;
	}

	// ===== UserDetails =====

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public String getPassword() {
		return password; // 🔥 ОБОВ’ЯЗКОВО
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities; // 🔥 ОБОВ’ЯЗКОВО
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}