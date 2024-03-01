package am.ik.blog.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.JsonRecyclerPools;

import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

	// https://github.com/spring-projects/spring-boot/issues/39783
	@Bean
	@ConditionalOnThreading(Threading.VIRTUAL)
	public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
		return builder -> builder
			.factory(JsonFactory.builder().recyclerPool(JsonRecyclerPools.sharedLockFreePool()).build());
	}

}
