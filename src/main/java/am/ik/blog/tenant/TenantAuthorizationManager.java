package am.ik.blog.tenant;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import am.ik.blog.security.Privilege;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public class TenantAuthorizationManager
		implements AuthorizationManager<RequestAuthorizationContext> {
	private final String resource;

	private final Set<Privilege> requiredPrivileges;

	public TenantAuthorizationManager(String resource, Privilege... requiredPrivileges) {
		this(resource, Set.of(requiredPrivileges));
	}

	public TenantAuthorizationManager(String resource,
			Set<Privilege> requiredPrivileges) {
		this.resource = resource;
		this.requiredPrivileges = requiredPrivileges;
	}

	@Override
	public AuthorizationDecision check(Supplier<Authentication> supplier,
			RequestAuthorizationContext context) {
		final String tenantId = context.getVariables().get("tenantId");
		final Set<? extends GrantedAuthority> authorities = new HashSet<>(
				supplier.get().getAuthorities());
		for (Privilege privilege : requiredPrivileges) {
			if (!authorities.contains(privilege.toAuthority(tenantId, resource))
					&& !authorities.contains(privilege.toAuthority("*", resource))) {
				return new AuthorizationDecision(false);
			}
		}
		return new AuthorizationDecision(true);
	}
}
