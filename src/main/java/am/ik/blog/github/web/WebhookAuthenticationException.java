package am.ik.blog.github.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception raised when a github webhook message is received but its HMAC signature does
 * not match the one computed with the shared secret.
 */
public class WebhookAuthenticationException extends ResponseStatusException {

	public WebhookAuthenticationException(String expected, String actual) {
		super(HttpStatus.FORBIDDEN,
				String.format("Could not verify signature: '%s'", actual));
	}
}