package am.ik.blog.config.r2dbc;

import brave.Tracer;
import io.r2dbc.proxy.ProxyConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class ConnectionFactoryPostProcessor implements BeanPostProcessor {
	private final Tracer tracer;

	public ConnectionFactoryPostProcessor(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ConnectionFactory) {
			return ProxyConnectionFactory.builder((ConnectionFactory) bean)
					.listener(new TracingExecutionListener(tracer, "blog:blog-db"))
					.build();
		}
		return bean;
	}
}
