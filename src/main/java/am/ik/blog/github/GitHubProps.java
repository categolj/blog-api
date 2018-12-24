package am.ik.blog.github;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "blog.github")
@Validated
public class GitHubProps implements org.springframework.validation.Validator {
	private String accessToken;
	private String webhookSecret;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getWebhookSecret() {
		return webhookSecret;
	}

	public void setWebhookSecret(String webhookSecret) {
		this.webhookSecret = webhookSecret;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == GitHubProps.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		Validator<GitHubProps> validator = Validator.builder(GitHubProps.class)
				.constraint(GitHubProps::getAccessToken, "accessToken",
						ContainerConstraintBase::notEmpty)
				.constraint(GitHubProps::getWebhookSecret, "webhookSecret",
						ContainerConstraintBase::notEmpty)
				.build();
		ConstraintViolations validate = validator.validate((GitHubProps) target);
		if (!validate.isValid()) {
			validate.apply(errors::rejectValue);
		}
	}
}