package am.ik.blog.tenant;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static java.util.stream.Collectors.toUnmodifiableMap;

public class TenantUserDetailsService implements UserDetailsService {

	private final Map<String, TenantUserDetails> users;

	public TenantUserDetailsService(TenantUserProps props) {
		this.users = Objects.<List<TenantUserDetails>>requireNonNullElseGet(props.users(), List::of)
			.stream()
			.collect(toUnmodifiableMap(TenantUserDetails::getUsername, Function.identity()));
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		final TenantUserDetails tenantUserDetails = this.users.get(username);
		if (tenantUserDetails == null) {
			throw new UsernameNotFoundException("The requested user (%s) is not found.".formatted(username));
		}
		return tenantUserDetails;
	}

}
