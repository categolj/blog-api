package am.ik.blog.github;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.BiValidator;
import am.ik.yavi.meta.ConstraintTarget;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "blog.github")
@Component
@Validated
public class GitHubProps implements org.springframework.validation.Validator {

    private String accessToken;

    private String webhookSecret;

	private final BiValidator<GitHubProps, Errors> validator = ValidatorBuilder.<GitHubProps>of()
			.constraint(GitHubProps::getAccessToken, "accessToken", c -> c.notBlank())
			.constraint(GitHubProps::getWebhookSecret, "webhookSecret", c -> c.notBlank())
			.build(Errors::rejectValue);

    @ConstraintTarget
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @ConstraintTarget
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
        this.validator.accept((GitHubProps) target, errors);
    }
}
