package am.ik.blog.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ExportMetricReader;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.metrics.aggregate.AggregateMetricReader;
import org.springframework.boot.actuate.metrics.export.MetricExportProperties;
import org.springframework.boot.actuate.metrics.reader.MetricReader;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.actuate.metrics.repository.redis.RedisMetricRepository;
import org.springframework.boot.actuate.metrics.writer.DefaultCounterService;
import org.springframework.boot.actuate.metrics.writer.DefaultGaugeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Profile("cloud")
@Configuration
public class MetricsConfig {

	@Autowired
	RedisConnectionFactory redisConnectionFactory;

	@Autowired
	MetricExportProperties export;

	// Metrics for an instance
	@Bean
	@ExportMetricReader
	MetricRepository metricRepository() {
		return new RedisMetricRepository(redisConnectionFactory,
				export.getRedis().getPrefix(), export.getRedis().getKey());
	}

	@Bean
	GaugeService gaugeWriter() {
		return new DefaultGaugeService(metricRepository());
	}

	@Bean
	CounterService counterService() {
		return new DefaultCounterService(metricRepository());
	}

	// Metrics for all instances
	@Bean
	MetricRepository aggregateMetricRepository() {
		return new RedisMetricRepository(redisConnectionFactory,
				export.getRedis().getAggregatePrefix(), export.getRedis().getKey());
	}

	@Bean
	@ExportMetricReader
	MetricReader aggregateMetricReader() {
		return new AggregateMetricReader(aggregateMetricRepository());
	}

}