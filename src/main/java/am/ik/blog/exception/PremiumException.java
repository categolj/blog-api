package am.ik.blog.exception;

import java.net.URI;

public class PremiumException extends RuntimeException {
	private final URI premiumUri;

	public PremiumException(URI premiumUri) {
		this.premiumUri = premiumUri;
	}

	public URI premiumUri() {
		return premiumUri;
	}
}
