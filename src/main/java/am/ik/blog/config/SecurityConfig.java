package am.ik.blog.config;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import am.ik.blog.config.SecurityConfig.RuntimeHints;
import am.ik.blog.security.CompositeUserDetailsService;
import am.ik.blog.security.Privilege;
import am.ik.blog.tenant.MethodInvocationTenantAuthorizationManager;
import am.ik.blog.tenant.RequestTenantAuthorizationManager;
import am.ik.blog.tenant.TenantUserDetails;
import am.ik.blog.tenant.TenantUserDetailsService;
import am.ik.blog.tenant.TenantUserProps;
import jakarta.annotation.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.util.ReflectionUtils;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ TenantUserProps.class })
@ImportRuntimeHints(RuntimeHints.class)
@EnableMethodSecurity(prePostEnabled = false)
public class SecurityConfig {

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public Advisor postAuthorize() {
		return AuthorizationManagerBeforeMethodInterceptor
			.preAuthorize(new MethodInvocationTenantAuthorizationManager());
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, CompositeUserDetailsService userDetailsService)
			throws Exception {
		final AuthorizationManager<RequestAuthorizationContext> listForTenant = new RequestTenantAuthorizationManager(
				"entry", Privilege.LIST);
		final AuthorizationManager<RequestAuthorizationContext> exportForTenant = new RequestTenantAuthorizationManager(
				"entry", Privilege.EXPORT);
		final AuthorizationManager<RequestAuthorizationContext> importForTenant = new RequestTenantAuthorizationManager(
				"entry", Privilege.IMPORT);
		final AuthorizationManager<RequestAuthorizationContext> getForTenant = new RequestTenantAuthorizationManager(
				"entry", Privilege.GET);
		final AuthorizationManager<RequestAuthorizationContext> editForTenant = new RequestTenantAuthorizationManager(
				"entry", Privilege.EDIT);
		final AuthorizationManager<RequestAuthorizationContext> deleteForTenant = new RequestTenantAuthorizationManager(
				"entry", Privilege.DELETE);
		return http.authorizeHttpRequests(authorize -> authorize //
			.requestMatchers("/admin/import")
			.hasAuthority("entry:import") //
			.requestMatchers("/entries.zip")
			.hasAuthority("entry:export") //
			.requestMatchers(POST, "/entries/**")
			.hasAuthority("entry:edit") //
			.requestMatchers(PATCH, "/entries/**")
			.hasAuthority("entry:edit") //
			.requestMatchers(PUT, "/entries/**")
			.hasAuthority("entry:edit") //
			.requestMatchers(DELETE, "/entries/**")
			.hasAuthority("entry:delete") //
			.requestMatchers(POST, "/tenants/{tenantId}/webhook")
			.permitAll() //
			.requestMatchers(GET, "/tenants/{tenantId}/entries.zip")
			.access(exportForTenant) //
			.requestMatchers(GET, "/tenants/{tenantId}/entries")
			.access(listForTenant) //
			.requestMatchers(GET, "/tenants/{tenantId}/entries/**")
			.access(getForTenant) //
			.requestMatchers(POST, "/tenants/{tenantId}/**")
			.access(editForTenant) //
			.requestMatchers(PATCH, "/tenants/{tenantId}/**")
			.access(editForTenant) //
			.requestMatchers(PUT, "/tenants/{tenantId}/**")
			.access(editForTenant) //
			.requestMatchers(DELETE, "/tenants/{tenantId}/**")
			.access(deleteForTenant) //
			.requestMatchers(POST, "/tenants/{tenantId}/admin/import")
			.access(importForTenant) //
			.anyRequest()
			.permitAll()) //
			.httpBasic(Customizer.withDefaults())
			.userDetailsService(userDetailsService)
			.csrf(AbstractHttpConfigurer::disable) //
			.cors(Customizer.withDefaults())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userDetailsService);
		authenticationProvider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(authenticationProvider);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	@Primary
	public CompositeUserDetailsService compositeUserDetailsService(List<UserDetailsService> userDetailsServices) {
		return new CompositeUserDetailsService(userDetailsServices);
	}

	@Bean
	@Order(1)
	public InMemoryUserDetailsManager inMemoryUserDetailsManager(SecurityProperties properties,
			PasswordEncoder passwordEncoder) {
		final SecurityProperties.User user = properties.getUser();
		final List<GrantedAuthority> authorities = user.getRoles()
			.stream()
			.map(Privilege::fromRole)
			.flatMap(Collection::stream)
			.flatMap(p -> Stream.of(p.toAuthority("entry"), p.toAuthority("*", "entry")))
			.toList();
		return new InMemoryUserDetailsManager(User.withUsername(user.getName())
			.password(passwordEncoder.encode(user.getPassword()))

			.authorities(authorities)
			.build());
	}

	@Bean
	@Order(2)
	public TenantUserDetailsService tenantUserDetailsService(TenantUserProps props) {
		return new TenantUserDetailsService(props);
	}

	public static class RuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(org.springframework.aot.hint.RuntimeHints hints, @Nullable ClassLoader classLoader) {
			hints.reflection()
				.registerMethod(
						Objects.requireNonNull(
								ReflectionUtils.findMethod(TenantUserDetails.class, "valueOf", String.class)),
						ExecutableMode.INVOKE);
		}

	}

}