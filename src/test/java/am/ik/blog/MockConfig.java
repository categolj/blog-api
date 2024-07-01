package am.ik.blog;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import am.ik.blog.entry.EntryMapper;
import am.ik.blog.entry.EntryService;
import io.micrometer.tracing.Tracer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockConfig {

	@Bean
	public EntryService entryService(EntryMapper entryMapper, Optional<Tracer> tracer) {
		return new EntryService(entryMapper, tracer);
	}

	@Bean
	public Clock fixedClock() {
		return Clock.fixed(OffsetDateTime.of(2022, 4, 1, 1, 0, 0, 0, ZoneOffset.UTC).toInstant(), ZoneId.of("UTC"));
	}

}
