package am.ik.blog.security;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CompositeUserDetailsService implements UserDetailsService {

	private final List<UserDetailsService> delegates;

	public CompositeUserDetailsService(List<UserDetailsService> delegates) {
		this.delegates = delegates;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		for (UserDetailsService delegate : delegates) {
			try {
				final UserDetails userDetails = delegate.loadUserByUsername(username);
				if (userDetails != null) {
					return userDetails;
				}
			}
			catch (UsernameNotFoundException ignored) {

			}
		}
		throw new UsernameNotFoundException("The requested user (%s) is not found.".formatted(username));
	}

}
