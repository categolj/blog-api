package am.ik.blog.tenant;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import am.ik.blog.security.Privilege;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.stream.Collectors.toMap;

public record TenantUserDetails(String username, String password,
								Map<String, List<Privilege>> privileges) implements UserDetails {

	public static TenantUserDetails valueOf(String value) {
		final String[] values = value.split("\\|", 3);
		final String username = values[0];
		final String password = values[1];
		return new TenantUserDetails(username, password, Arrays.stream(values[2].split("\\|"))
				.map(s -> {
					final String[] p = s.split("=");
					final String tenantId = p[0];
					final String[] privileges = p[1].split(",");
					return Tuples.of(tenantId, Arrays.stream(privileges).map(Privilege::valueOf).toList());
				})
				.collect(toMap(Tuple2::getT1, Tuple2::getT2)));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.privileges.entrySet().stream()
				.flatMap(e -> e.getValue().stream()
						.map(p -> p.toAuthority(e.getKey(), "entry")))
				.toList();
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
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
