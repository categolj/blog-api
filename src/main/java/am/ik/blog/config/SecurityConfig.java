package am.ik.blog.config;

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@Lazy
public class SecurityConfig {
	@Bean
	public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
		return http //
				.httpBasic() //
				.and() //
				.authorizeExchange(authorization -> authorization //
						.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() //
						.matchers(EndpointRequest.to("health", "info")).permitAll() //
						.matchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR") //
						.pathMatchers("/admin/**").hasRole("ACTUATOR") //
						.anyExchange().permitAll()) //
				.csrf(csrf -> csrf.disable()) //
				.build();
	}
}
