package am.ik.blog.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@Order(-5)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.requestMatcher(EndpointRequest.toAnyEndpoint()) //
				.authorizeRequests() //
				.requestMatchers(EndpointRequest.to("health", "info")).permitAll() //
				.requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR") //
				.and() //
				.httpBasic() //
				.and() //
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
	}
}
