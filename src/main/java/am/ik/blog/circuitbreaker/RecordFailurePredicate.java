package am.ik.blog.circuitbreaker;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class RecordFailurePredicate implements Predicate<Throwable> {
	private final Logger log = LoggerFactory.getLogger(RecordFailurePredicate.class);

	@Override
	public boolean test(Throwable throwable) {
		if (throwable instanceof WebClientResponseException) {
			final WebClientResponseException ex = (WebClientResponseException) throwable;
			final HttpStatusCode statusCode = ex.getStatusCode();
			if (statusCode.is4xxClientError()) {
				return false;
			}
		}
		log.warn(">> Record failure: " + throwable);
		return true;
	}
}
