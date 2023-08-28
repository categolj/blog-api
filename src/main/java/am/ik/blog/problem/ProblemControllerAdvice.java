package am.ik.blog.problem;

import am.ik.webhook.WebhookAuthenticationException;
import am.ik.yavi.core.ConstraintViolationsException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Optional;

@RestControllerAdvice
public class ProblemControllerAdvice {

	private final Logger log = LoggerFactory.getLogger(ProblemControllerAdvice.class);

	private final Tracer tracer;

	public ProblemControllerAdvice(Optional<Tracer> tracer) {
		this.tracer = tracer.orElseGet(() -> {
			log.warn("Tracer is not found. NOOP trace is used instead.");
			return Tracer.NOOP; /* for test */
		});
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ProblemDetail handleResponseStatusException(ResponseStatusException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatus(e.getStatusCode());
		problemDetail.setDetail(e.getReason());
		return setTraceId(problemDetail);
	}

	@ExceptionHandler(ConstraintViolationsException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ProblemDetail handleConstraintViolationsException(ConstraintViolationsException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Constraint violations found!");
		problemDetail.setProperty("violations", e.violations().details());
		return setTraceId(problemDetail);
	}

	@ExceptionHandler(WebhookAuthenticationException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ProblemDetail handleWebhookAuthenticationException(WebhookAuthenticationException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
		return setTraceId(problemDetail);
	}

	@ExceptionHandler(DataAccessException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ProblemDetail handleDataAccessException(DataAccessException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
				"There is a problem with database access.");
		log.error("There is a problem with database access.", e);
		return setTraceId(problemDetail);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail handleNoResourceFoundException(NoResourceFoundException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		return setTraceId(problemDetail);
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ProblemDetail handleAccessDeniedException(AccessDeniedException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
		return setTraceId(problemDetail);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ProblemDetail handleRuntimeException(RuntimeException e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Unexpected runtime error occurred!");
		log.error("Unexpected runtime error occurred!", e);
		return setTraceId(problemDetail);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ProblemDetail handleException(Exception e) {
		final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Unexpected error occurred!");
		log.error("Unexpected error occurred!", e);
		return setTraceId(problemDetail);
	}

	private ProblemDetail setTraceId(ProblemDetail problemDetail) {
		final Span currentSpan = tracer.currentSpan();
		if (currentSpan != null) {
			problemDetail.setProperty("traceId", currentSpan.context().traceId());
		}
		return problemDetail;
	}

}
