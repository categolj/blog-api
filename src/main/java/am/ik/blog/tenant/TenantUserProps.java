package am.ik.blog.tenant;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "blog.tenant")
public record TenantUserProps(
		@NestedConfigurationProperty List<TenantUserDetails> users) {
}
