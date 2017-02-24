package am.ik.blog.api;

import am.ik.categolj3.api.EnableCategoLJ3ApiServer;
import am.ik.categolj3.api.entry.redis.EntryRedisTemplateFactory;
import am.ik.categolj3.api.git.GitProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
@EnableCategoLJ3ApiServer
@EnableDiscoveryClient
@EnableConfigurationProperties(GitProperties.class)
public class BlogApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApiApplication.class, args);
    }

    @Profile("cloud")
    @Bean
    RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        return new EntryRedisTemplateFactory(redisConnectionFactory, objectMapper).create();
    }
}
