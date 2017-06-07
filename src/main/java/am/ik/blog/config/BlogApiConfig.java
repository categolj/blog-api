package am.ik.blog.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BlogApiConfig {
	@Bean
	@LoadBalanced
	RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	@LoadBalanced
	OAuth2RestTemplate oAuth2RestTemplate(OAuth2ProtectedResourceDetails resource,
			OAuth2ClientContext context, RestTemplate restTemplate) {
		OAuth2RestTemplate oauth2RestTemplate = new OAuth2RestTemplate(resource, context);
		oauth2RestTemplate.setRequestFactory(restTemplate.getRequestFactory());
		return oauth2RestTemplate;
	}

}
