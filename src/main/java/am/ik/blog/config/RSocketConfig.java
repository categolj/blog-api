package am.ik.blog.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.rsocket.micrometer.MicrometerRSocketInterceptor;
import org.springframework.boot.rsocket.server.ServerRSocketFactoryProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RSocketConfig {

    @Bean
    public ServerRSocketFactoryProcessor serverRSocketFactoryProcessor(MeterRegistry meterRegistry) {
        return factory -> factory.addResponderPlugin(new MicrometerRSocketInterceptor(meterRegistry));
    }
}
