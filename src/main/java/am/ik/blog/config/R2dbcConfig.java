package am.ik.blog.config;

import java.time.Duration;

import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import static io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.STATEMENT_TIMEOUT;
import static io.r2dbc.spi.ConnectionFactoryOptions.CONNECT_TIMEOUT;

@Configuration
public class R2dbcConfig {
	private final Logger log = LoggerFactory.getLogger(R2dbcConfig.class);

	@Bean
	@Lazy(false)
	public InitializingBean initializer(ConnectionFactory connectionFactory, @Value("${script.location:classpath://schema.sql}") Resource script) {
		final ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		final ResourceDatabasePopulator populator = new ResourceDatabasePopulator(script);
		initializer.setDatabasePopulator(populator);
		return () -> {
			try {
				initializer.afterPropertiesSet();
			}
			catch (Exception e) {
				log.warn("Database initializer filed: " + e);
			}
		};
	}

	@Bean
	public ConnectionFactoryOptionsBuilderCustomizer connectionFactoryOptionsBuilderCustomizer() {
		return builder -> builder
				.option(CONNECT_TIMEOUT, Duration.ofSeconds(10))
				.option(STATEMENT_TIMEOUT, Duration.ofSeconds(10));
	}
}