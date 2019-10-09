package am.ik.blog.config;

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactorNettyConfig {
	@Bean
	public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> customizer() {
		return factory -> factory.addServerCustomizers(builder -> builder.metrics(true));
	}
}