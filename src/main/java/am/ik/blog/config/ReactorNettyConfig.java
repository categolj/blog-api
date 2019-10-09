package am.ik.blog.config;

import reactor.netty.http.client.HttpClient;

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ReactorNettyConfig {
	@Bean
	public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> customizer() {
		return factory -> factory.addServerCustomizers(builder -> builder.metrics(true));
	}

	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder().clientConnector(
				new ReactorClientHttpConnector(HttpClient.create().tcpConfiguration(builder -> builder.metrics(true))));
	}
}