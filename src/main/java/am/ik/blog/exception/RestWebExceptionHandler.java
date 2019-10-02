package am.ik.blog.exception;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import reactor.core.publisher.Mono;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestWebExceptionHandler implements WebExceptionHandler {
	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		if (ex instanceof NumberFormatException) {
			Matcher matcher = Pattern.compile("For input string: \"(.+)\"").matcher(ex.getMessage());
			if (matcher.find()) {
				String variableName = matcher.group(1);
				return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("The given request (%s) is not valid.", variableName), ex));
			}
		}
		if (ex instanceof CallNotPermittedException) {
			return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"The service is temporarily unavailable.", ex));
		}
		return Mono.error(ex);
	}
}
