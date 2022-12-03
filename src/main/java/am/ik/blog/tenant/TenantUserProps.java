package am.ik.blog.tenant;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blog.tenant")
public record TenantUserProps(List<TenantUserDetails> users) {
}
