package am.ik.blog.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.context.annotation.Bean;

@ManagementContextConfiguration
public class AcceptTrailingSlashFilterConfig {
	@Bean
	public Filter acceptTrailingSlashFilter() {
		return new GenericFilter() {
			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
				final String uri = ((HttpServletRequest) request).getRequestURI();
				if (uri != null && uri.endsWith("/")) {
					filterChain.doFilter(new HttpServletRequestWrapper((HttpServletRequest) request) {
						@Override
						public String getRequestURI() {
							return uri.substring(0, uri.length() - 1);
						}
					}, response);
				}
				else {
					filterChain.doFilter(request, response);
				}
			}
		};
	}
}
