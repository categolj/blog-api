package am.ik.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.DisableEncodeUrlFilter;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
	private final UriFilter uriFilter;

	public SecurityConfig(UriFilter uriFilter) {
		this.uriFilter = uriFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/admin/**").authenticated()
						.anyRequest().permitAll()
				)
				.httpBasic()
				.and()
				.csrf(AbstractHttpConfigurer::disable)
				.cors()
				.and()
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(new RequestLoggingFilter(uriFilter), DisableEncodeUrlFilter.class)
				.build();
	}
}