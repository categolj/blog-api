package am.ik.blog.config;

import org.springframework.boot.autoconfigure.web.embedded.TomcatVirtualThreadsWebServerFactoryCustomizer;
import org.springframework.boot.system.JavaVersion;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Remove when updating to Java 21
@Configuration(proxyBeanMethods = false)
public class VirtualThreadsConfig {

	@Bean
	public WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> tomcatVirtualThreadsProtocolHandlerCustomizer() {
		if (JavaVersion.getJavaVersion().equals(JavaVersion.TWENTY)) {
			return new TomcatVirtualThreadsWebServerFactoryCustomizer();
		}
		else {
			return factory -> {
			};
		}
	}

}
