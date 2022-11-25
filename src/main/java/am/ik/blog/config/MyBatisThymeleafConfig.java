package am.ik.blog.config;

import java.util.List;
import java.util.Objects;

import am.ik.blog.config.MyBatisThymeleafConfig.RuntimeHints;
import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.mybatis.scripting.thymeleaf.SqlGeneratorConfig;
import org.mybatis.scripting.thymeleaf.expression.Likes;
import org.thymeleaf.expression.Lists;
import org.thymeleaf.expression.Strings;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.util.ReflectionUtils;

import static org.mybatis.scripting.thymeleaf.processor.BindVariableRender.BuiltIn.SPRING_NAMED_PARAMETER;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(RuntimeHints.class)
public class MyBatisThymeleafConfig {
	@Bean
	public SqlGenerator sqlGenerator() {
		final SqlGeneratorConfig config = SqlGeneratorConfig
				.newInstanceWithCustomizer(c -> c.getDialect()
						.setBindVariableRenderInstance(SPRING_NAMED_PARAMETER));
		return new SqlGenerator(config);
	}

	public static class RuntimeHints implements RuntimeHintsRegistrar {
		@Override
		public void registerHints(org.springframework.aot.hint.RuntimeHints hints,
				ClassLoader classLoader) {
			hints.reflection()
					.registerMethod(Objects.requireNonNull(ReflectionUtils
							.findMethod(Lists.class, "isEmpty", List.class)),
							ExecutableMode.INVOKE)
					.registerMethod(
							Objects.requireNonNull(ReflectionUtils.findMethod(
									Strings.class, "toLowerCase", Object.class)),
							ExecutableMode.INVOKE)
					.registerMethod(
							Objects.requireNonNull(ReflectionUtils.findMethod(Likes.class,
									"escapeWildcard", String.class)),
							ExecutableMode.INVOKE);
			hints.resources().registerPattern("am/ik/blog/*");
		}
	}
}
