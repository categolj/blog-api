package am.ik.blog.config;

import java.net.URI;
import java.time.Duration;

import brave.Tracer;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.proxy.ProxyConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class R2dbcConfig {
	@Bean
	public ConnectionFactory connectionFactory(
			@Value("${spring.datasource.url}") String uriString,
			@Value("${spring.datasource.username}") String username,
			@Value("${spring.datasource.password}") String password, Tracer tracer) {
		final URI uri = URI.create(uriString.replace("jdbc:", ""));
		final PostgresqlConnectionFactory connectionFactory = new PostgresqlConnectionFactory(
				PostgresqlConnectionConfiguration.builder() //
						.host(uri.getHost()) //
						.port(uri.getPort()) //
						.username(username) //
						.password(password) //
						.database(uri.getPath().replace("/", "")) //
						.build());
		final ConnectionFactory factory = ProxyConnectionFactory
				.builder(connectionFactory) //
				.listener(new TracingExecutionListener(tracer)) //
				.build();
		return new ConnectionPool(ConnectionPoolConfiguration.builder(factory) //
				.maxSize(40) //
				.maxIdleTime(Duration.ofSeconds(5)) //
				.validationQuery("SELECT 1") //
				.build());
	}

	@Bean
	public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
		return DatabaseClient.builder().connectionFactory(connectionFactory).build();
	}

	@Bean
	public TransactionalOperator transactionalOperator(
			ConnectionFactory connectionFactory) {
		return TransactionalOperator
				.create(new R2dbcTransactionManager(connectionFactory));
	}
}
