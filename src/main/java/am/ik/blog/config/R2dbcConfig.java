package am.ik.blog.config;

import java.net.URI;

import brave.Tracer;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import net.ttddyy.dsproxy.r2dbc.ProxyConnectionFactoryBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.function.TransactionalDatabaseClient;

@Configuration
public class R2dbcConfig {
	@Bean
	public ConnectionFactory connectionFactory(
			@Value("${spring.datasource.url}") String uriString,
			@Value("${spring.datasource.username}") String username,
			@Value("${spring.datasource.password}") String password, Tracer tracer) {
		URI uri = URI.create(uriString.replace("jdbc:", ""));
		PostgresqlConnectionFactory connectionFactory = new PostgresqlConnectionFactory(
				PostgresqlConnectionConfiguration.builder() //
						.host(uri.getHost()) //
						.port(uri.getPort()) //
						.username(username) //
						.password(password) //
						.database(uri.getPath().replace("/", "")) //
						.build());
		return ProxyConnectionFactoryBuilder.create(connectionFactory) //
				.listener(new TracingExecutionListener(tracer)) //
				.build();
	}

	@Bean
	public TransactionalDatabaseClient databaseClient(
			ConnectionFactory connectionFactory) {
		return TransactionalDatabaseClient.create(connectionFactory);
	}
}
