package am.ik.blog.config;

import org.mybatis.scripting.thymeleaf.SqlGenerator;
import org.mybatis.scripting.thymeleaf.SqlGeneratorConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mybatis.scripting.thymeleaf.processor.BindVariableRender.BuiltIn.SPRING_NAMED_PARAMETER;

@Configuration
public class MyBatisConfig {
	@Bean
	public SqlGenerator sqlGenerator() {
		final SqlGeneratorConfig config = SqlGeneratorConfig.newInstanceWithCustomizer(c ->
				c.getDialect().setBindVariableRender(SPRING_NAMED_PARAMETER.getType()));
		return new SqlGenerator(config);
	}
}
