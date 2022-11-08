package am.ik.blog.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import am.ik.blog.config.OtlpProperties.BasicAuth;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
public class OtelConfig {
	@Bean
	@ConditionalOnProperty(name = "management.otlp.endpoint")
	public OtlpGrpcSpanExporter otelOtlpGrpcSpanExporter(OtlpProperties properties) {
		final OtlpGrpcSpanExporterBuilder builder = OtlpGrpcSpanExporter.builder();
		final String endpoint = properties.getEndpoint();
		if (StringUtils.hasText(endpoint)) {
			builder.setEndpoint(endpoint);
		}
		final Long timeout = properties.getTimeout();
		if (timeout != null) {
			builder.setTimeout(timeout, TimeUnit.MILLISECONDS);
		}
		final Map<String, String> headers = properties.getHeaders();
		if (!headers.isEmpty()) {
			headers.forEach(builder::addHeader);
		}
		final BasicAuth basicAuth = properties.getBasicAuth();
		if (basicAuth.isEnabled()) {
			final String basic = "%s:%s".formatted(basicAuth.getUsername(), basicAuth.getPassword());
			builder.addHeader(HttpHeaders.AUTHORIZATION, "Basic %s".formatted(Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8))));
		}
		return builder.build();
	}

	@Bean
	public BeanPostProcessor uriFilteringSpanExporterRegistrar(UriFilter uriFilter) {
		return new BeanPostProcessor() {
			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				if (bean instanceof final SpanExporter spanExporter) {
					return new HttpUrlFilteringSpanExporter(spanExporter, uriFilter);
				}
				return bean;
			}
		};
	}
}
