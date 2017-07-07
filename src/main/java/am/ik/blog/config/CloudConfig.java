package am.ik.blog.config;

import javax.sql.DataSource;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.service.relational.DataSourceConfig;
import org.springframework.cloud.service.relational.DataSourceConfig.ConnectionConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("cloud")
public class CloudConfig extends AbstractCloudConfig {

	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.tomcat")
	DataSource dataSource() {
		return connectionFactory().dataSource(new DataSourceConfig(null,
				new ConnectionConfig("allowMultiQueries=true")));
	}

	// @Bean
	// ConnectionFactory rabbitConnectionFactory() {
	// 	return connectionFactory().rabbitConnectionFactory();
	// }
}
