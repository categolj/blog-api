package am.ik.blog.exception;

import java.net.URI;

public class PremiumRedirectException extends RuntimeException {
	private final URI premiumUri;

	public PremiumRedirectException(URI premiumUri) {
		this.premiumUri = premiumUri;
	}

	public URI premiumUri() {
		return premiumUri;
	}
}
