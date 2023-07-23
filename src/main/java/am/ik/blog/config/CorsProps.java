package am.ik.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "blog.cors")
public record CorsProps(@DefaultValue("*") String allowedOrigins) {
}
