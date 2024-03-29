package am.ik.blog.security;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import am.ik.blog.config.WebConfig;

import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Privilege {

	GET, LIST, EDIT, DELETE, IMPORT, EXPORT;

	public static Set<Privilege> fromRole(String role) {
		if ("ADMIN".equalsIgnoreCase(role)) {
			return EnumSet.allOf(Privilege.class);
		}
		if ("EDITOR".equalsIgnoreCase(role)) {
			return EnumSet.of(GET, LIST, EDIT);
		}
		if ("VIEWER".equalsIgnoreCase(role)) {
			return EnumSet.of(GET, LIST);
		}
		return Set.of();
	}

	public GrantedAuthority toAuthority(@Nullable String resource) {
		return new SimpleGrantedAuthority("%s:%s".formatted(resource, this.toString()));
	}

	public GrantedAuthority toAuthority(@Nullable String tenantId, @Nullable String resource) {
		return new SimpleGrantedAuthority("%s:%s:%s".formatted(tenantId, resource, this.toString()));
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.US);
	}

}
