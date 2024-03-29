package am.ik.blog.tenant;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import am.ik.blog.security.Privilege;
import am.ik.blog.util.Tuple2;

import org.springframework.lang.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public abstract class AbstractTenantAuthorizationManager<T> implements AuthorizationManager<T> {

	@Nullable
	protected abstract String tenantId(T context);

	@Nullable
	protected abstract Tuple2<String, Set<Privilege>> resourceAndPrivileges(T context);

	protected boolean isPermitted(@Nullable String tenantId, @Nullable String resource, Set<Privilege> privileges) {
		return false;
	}

	@Override
	public final AuthorizationDecision check(Supplier<Authentication> supplier, T context) {
		final String tenantId = this.tenantId(context);
		final Tuple2<String, Set<Privilege>> tuple2 = this.resourceAndPrivileges(context);
		final Set<? extends GrantedAuthority> authorities = new HashSet<>(supplier.get().getAuthorities());
		final String resource = tuple2 == null ? null : tuple2.getT1();
		final Set<Privilege> privileges = tuple2 == null ? Collections.emptySet() : tuple2.getT2();
		if (isPermitted(tenantId, resource, privileges)) {
			return new AuthorizationDecision(true);
		}
		for (Privilege privilege : privileges) {
			if (!authorities.contains(privilege.toAuthority(tenantId, resource))
					&& !authorities.contains(privilege.toAuthority("*", resource))) {
				return new AuthorizationDecision(false);
			}
		}
		return new AuthorizationDecision(true);
	}

}
