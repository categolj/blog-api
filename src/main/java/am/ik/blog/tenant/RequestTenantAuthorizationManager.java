package am.ik.blog.tenant;

import java.util.Set;

import am.ik.blog.security.Privilege;
import am.ik.blog.util.Tuple2;
import am.ik.blog.util.Tuples;

import org.springframework.lang.Nullable;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public class RequestTenantAuthorizationManager extends AbstractTenantAuthorizationManager<RequestAuthorizationContext> {

	private final Tuple2<String, Set<Privilege>> resourceAndPrivileges;

	public RequestTenantAuthorizationManager(String resource, Privilege... requiredPrivileges) {
		this(resource, Set.of(requiredPrivileges));
	}

	public RequestTenantAuthorizationManager(String resource, Set<Privilege> requiredPrivileges) {
		this.resourceAndPrivileges = Tuples.of(resource, requiredPrivileges);
	}

	@Override
	@Nullable
	protected String tenantId(RequestAuthorizationContext context) {
		return context.getVariables().get("tenantId");
	}

	@Override
	protected Tuple2<String, Set<Privilege>> resourceAndPrivileges(RequestAuthorizationContext context) {
		return this.resourceAndPrivileges;
	}

}
