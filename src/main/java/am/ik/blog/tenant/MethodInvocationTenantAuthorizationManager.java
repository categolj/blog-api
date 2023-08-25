package am.ik.blog.tenant;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Set;

import am.ik.blog.security.Privilege;
import am.ik.blog.util.Tuple2;
import am.ik.blog.util.Tuples;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class MethodInvocationTenantAuthorizationManager
		extends AbstractTenantAuthorizationManager<MethodInvocation> {
	@Override
	protected boolean isPermitted(String tenantId, String resource,
			Set<Privilege> privileges) {
		if (tenantId != null) {
			return false;
		}
		if (!"entry".equals(resource)) {
			return false;
		}
		int size = privileges.size();
		if (size == 1) {
			return privileges.contains(Privilege.LIST)
					|| privileges.contains(Privilege.GET);
		}
		else if (size == 2) {
			return privileges.contains(Privilege.LIST)
					&& privileges.contains(Privilege.GET);
		}
		return false;
	}

	@Override
	protected String tenantId(MethodInvocation context) {
		final Parameter[] parameters = context.getMethod().getParameters();
		int i;
		for (i = 0; i < parameters.length; i++) {
			final Parameter parameter = parameters[i];
			final P p = parameter.getAnnotation(P.class);
			if (p != null && "tenantId".equals(p.value())) {
				break;
			}
		}
		if (i < parameters.length) {
			return (String) context.getArguments()[i];
		}
		return null;
	}

	@Override
	protected Tuple2<String, Set<Privilege>> resourceAndPrivileges(
			MethodInvocation context) {
		final PreAuthorize preAuthorize = context.getMethod()
				.getAnnotation(PreAuthorize.class);
		if (preAuthorize != null && preAuthorize.value().contains("=")) {
			final String[] strings = preAuthorize.value().split("=", 2);
			final String resource = strings[0];
			final String[] privileges = strings[1].split(",");
			return Tuples.of(resource, Arrays.stream(privileges).map(Privilege::valueOf)
					.collect(toUnmodifiableSet()));
		}
		return null;
	}
}
