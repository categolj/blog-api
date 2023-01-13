package am.ik.blog.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import am.ik.accesslogger.AccessLogger;
import am.ik.blog.security.CompositeUserDetailsService;
import am.ik.blog.security.Privilege;
import am.ik.blog.tenant.TenantAuthorizationManager;
import am.ik.blog.tenant.TenantUserDetailsService;
import am.ik.blog.tenant.TenantUserProps;

import org.springframework.boot.actuate.autoconfigure.web.exchanges.HttpExchangesProperties;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.servlet.HttpExchangesFilter;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
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
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ TenantUserProps.class, HttpExchangesProperties.class })
public class SecurityConfig {

	@Bean
	public AccessLogger accessLogger() {
		final UriFilter uriFilter = new UriFilter();
		return new AccessLogger(httpExchange -> uriFilter.test(httpExchange.getRequest().getUri().toString()));
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			CompositeUserDetailsService userDetailsService, HttpExchangeRepository repository, HttpExchangesProperties properties) throws Exception {
		final TenantAuthorizationManager listForTenant = new TenantAuthorizationManager(
				"entry", Privilege.LIST);
		final TenantAuthorizationManager exportForTenant = new TenantAuthorizationManager(
				"entry", Privilege.EXPORT);
		final TenantAuthorizationManager importForTenant = new TenantAuthorizationManager(
				"entry", Privilege.IMPORT);
		final TenantAuthorizationManager getForTenant = new TenantAuthorizationManager(
				"entry", Privilege.GET);
		final TenantAuthorizationManager editForTenant = new TenantAuthorizationManager(
				"entry", Privilege.EDIT);
		final TenantAuthorizationManager deleteForTenant = new TenantAuthorizationManager(
				"entry", Privilege.DELETE);
		return http.authorizeHttpRequests(authorize -> authorize //
				.requestMatchers("/admin/import").hasAuthority("entry:import") //
				.requestMatchers("/entries.zip").hasAuthority("entry:export") //
				.requestMatchers(POST, "/entries/**").hasAuthority("entry:edit") //
				.requestMatchers(PATCH, "/entries/**").hasAuthority("entry:edit") //
				.requestMatchers(PUT, "/entries/**").hasAuthority("entry:edit") //
				.requestMatchers(DELETE, "/entries/**").hasAuthority("entry:delete") //
				.requestMatchers(POST, "/tenants/{tenantId}/webhook").permitAll() //
				.requestMatchers(GET, "/tenants/{tenantId}/entries.zip")
				.access(exportForTenant) //
				.requestMatchers(GET, "/tenants/{tenantId}/entries").access(listForTenant) //
				.requestMatchers(GET, "/tenants/{tenantId}/entries/**")
				.access(getForTenant) //
				.requestMatchers(POST, "/tenants/{tenantId}/**").access(editForTenant) //
				.requestMatchers(PATCH, "/tenants/{tenantId}/**").access(editForTenant) //
				.requestMatchers(PUT, "/tenants/{tenantId}/**").access(editForTenant) //
				.requestMatchers(DELETE, "/tenants/{tenantId}/**").access(deleteForTenant) //
				.requestMatchers(POST, "/tenants/{tenantId}/admin/import")
				.access(importForTenant) //
				.anyRequest().permitAll()) //
				.httpBasic(Customizer.withDefaults())
				.userDetailsService(userDetailsService)
				.csrf(AbstractHttpConfigurer::disable) //
				.cors(Customizer.withDefaults())
				.sessionManagement(
						s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterAfter(new HttpExchangesFilter(repository, properties.getRecording().getInclude()),
						SecurityContextHolderAwareRequestFilter.class)
				.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(
			UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
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
	public CompositeUserDetailsService compositeUserDetailsService(
			List<UserDetailsService> userDetailsServices) {
		return new CompositeUserDetailsService(userDetailsServices);
	}

	@Bean
	@Order(1)
	public InMemoryUserDetailsManager inMemoryUserDetailsManager(
			SecurityProperties properties, PasswordEncoder passwordEncoder) {
		final SecurityProperties.User user = properties.getUser();
		final List<GrantedAuthority> authorities = user.getRoles().stream()
				.map(Privilege::fromRole).flatMap(Collection::stream).flatMap(p -> Stream
						.of(p.toAuthority("entry"), p.toAuthority("*", "entry")))
				.toList();
		return new InMemoryUserDetailsManager(User.withUsername(user.getName())
				.password(passwordEncoder.encode(user.getPassword()))

				.authorities(authorities).build());
	}

	@Bean
	@Order(2)
	public TenantUserDetailsService tenantUserDetailsService(TenantUserProps props) {
		return new TenantUserDetailsService(props);
	}
}