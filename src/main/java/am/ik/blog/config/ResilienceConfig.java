package am.ik.blog.config;

import java.util.Objects;

import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.event.RetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ResilienceConfig {

	@Bean
	public RegistryEventConsumer<Retry> myRetryRegistryEventConsumer() {
		return new RetryRegistryEventConsumer();
	}

	static class RetryRegistryEventConsumer implements RegistryEventConsumer<Retry> {

		private final Logger logger = LoggerFactory.getLogger(RetryRegistryEventConsumer.class);

		@Override
		public void onEntryAddedEvent(EntryAddedEvent<Retry> entryAddedEvent) {
			entryAddedEvent.getAddedEntry()
				.getEventPublisher()
				.onEvent(event -> log(event,
						"name={}\tevent_type={}\tattempt={}\tlast_throwable_type={}\tlast_throwable_message=\"{}\"",
						event.getName(), event.getEventType(), event.getNumberOfRetryAttempts(),
						event.getLastThrowable().getClass(),
						Objects.requireNonNullElse(event.getLastThrowable().getMessage(), "")));
		}

		void log(RetryEvent event, String format, Object... args) {
			switch (event.getEventType()) {
				case RETRY:
					logger.warn(format, args);
					break;
				case ERROR:
					logger.error(format, args);
					break;
				case SUCCESS:
				case IGNORED_ERROR:
					logger.info(format, args);
					break;
			}
		}

		@Override
		public void onEntryRemovedEvent(EntryRemovedEvent<Retry> entryRemoveEvent) {

		}

		@Override
		public void onEntryReplacedEvent(EntryReplacedEvent<Retry> entryReplacedEvent) {

		}

	}

}
