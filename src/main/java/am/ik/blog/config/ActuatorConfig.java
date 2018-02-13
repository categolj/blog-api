package am.ik.blog.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
@ConfigurationProperties("security")
@Order(-5)
public class ActuatorConfig extends WebSecurityConfigurerAdapter {
	private User user = new User();

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.requestMatcher(EndpointRequest.toAnyEndpoint()) //
				.authorizeRequests() //
				.requestMatchers(EndpointRequest.to("health", "info")).permitAll() //
				.requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR") //
				.and().httpBasic();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		UserDetails details = org.springframework.security.core.userdetails.User
				.withDefaultPasswordEncoder() //
				.username(this.user.name) //
				.password(this.user.password) //
				.roles("ACTUATOR") //
				.build();
		auth.inMemoryAuthentication() //
				.withUser(details);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static class User {
		private String name = "user";
		private String password = "password";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
}
