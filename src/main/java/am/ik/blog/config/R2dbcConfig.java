package am.ik.blog.config;

import io.r2dbc.spi.ConnectionFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
public class R2dbcConfig {
	@Bean
	@Lazy(false)
	public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory, @Value("${script.location:classpath://schema.sql}") Resource script) {
		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator(script);
		initializer.setDatabasePopulator(populator);
		return initializer;
	}
}