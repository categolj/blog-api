package am.ik.blog.httptrace;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import io.r2dbc.postgresql.codec.Json;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.IdGenerator;

@Repository
public class HttpTraceMapper {
	private final DatabaseClient databaseClient;

	private final TransactionalOperator transactionalOperator;

	private final ObjectMapper objectMapper;

	private final IdGenerator idGenerator;

	private final Clock clock;

	public HttpTraceMapper(DatabaseClient databaseClient, TransactionalOperator transactionalOperator, ObjectMapper objectMapper, IdGenerator idGenerator, Clock clock) {
		this.databaseClient = databaseClient;
		this.transactionalOperator = transactionalOperator;
		this.objectMapper = objectMapper;
		this.idGenerator = idGenerator;
		this.clock = clock;
	}

	public Flux<HttpTrace> findAll() {
		return this.databaseClient.sql("SELECT trace FROM http_trace ORDER BY id DESC")
				.map(row -> {
					final byte[] trace = row.get("trace", byte[].class);
					try {
						return this.objectMapper.readValue(trace, HttpTrace.class);
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				})
				.all();
	}

	public Mono<Integer> insert(HttpTrace trace) {
		try {
			final byte[] json = this.objectMapper.writeValueAsBytes(trace);
			return this.databaseClient.sql("INSERT INTO http_trace (id, trace) VALUES($1, $2)")
					.bind("$1", idGenerator.generateId())
					.bind("$2", Json.of(json))
					.fetch().rowsUpdated()
					.as(this.transactionalOperator::transactional);
		}
		catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	public Mono<Integer> dropOldTraces() {
		final Instant now = Instant.now(this.clock);
		final Instant oneDayAgo = now.minus(1, ChronoUnit.DAYS);
		final UUID id = UuidCreator.getTimeOrdered(oneDayAgo, 1, 2L);
		return this.databaseClient.sql("DELETE FROM http_trace WHERE id < $1")
				.bind("$1", id)
				.fetch().rowsUpdated()
				.as(this.transactionalOperator::transactional);
	}
}
