package am.ik.blog.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * OTLP settings for OpenTelemetry.
 *
 * @author Marcin Grzejszczak
 */
@ConfigurationProperties("management.otlp")
@Component
public class OtlpProperties {
	public static class BasicAuth {
		private String username;

		private String password;

		public boolean isEnabled() {
			return StringUtils.hasText(username) && StringUtils.hasText(password);
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	/**
	 * Enables OTLP exporter.
	 */
	private boolean enabled = true;

	/**
	 * Timeout in millis.
	 */
	private Long timeout;

	/**
	 * Sets the OTLP endpoint to connect to.
	 */
	private String endpoint;

	/**
	 * Map of headers to be added.
	 */
	private Map<String, String> headers = new HashMap<>();

	@NestedConfigurationProperty
	private BasicAuth basicAuth = new BasicAuth();

	public Long getTimeout() {
		return this.timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public String getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public BasicAuth getBasicAuth() {
		return basicAuth;
	}

	public void setBasicAuth(BasicAuth basicAuth) {
		this.basicAuth = basicAuth;
	}
}
