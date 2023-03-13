package am.ik.blog.config;

import am.ik.blog.config.NativeHints.RuntimeHints;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(RuntimeHints.class)
public class NativeHints {

	public static class RuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(org.springframework.aot.hint.RuntimeHints hints,
				ClassLoader classLoader) {
			try {
				hints.reflection().registerConstructor(
						org.flywaydb.core.internal.logging.slf4j.Slf4jLogCreator.class
								.getConstructor(),
						ExecutableMode.INVOKE);
				hints.reflection().registerConstructor(
						org.apache.tomcat.util.modeler.modules.MbeansDescriptorsIntrospectionSource.class
								.getConstructor(),
						ExecutableMode.INVOKE);
				// https://github.com/oracle/graal/issues/5626
				hints.reflection().registerConstructor(
						org.hibernate.validator.internal.util.logging.Log_$logger.class
								.getConstructor(org.jboss.logging.Logger.class),
						ExecutableMode.INVOKE).registerField(
								org.hibernate.validator.internal.util.logging.Messages_$bundle.class
										.getField("INSTANCE"));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			hints.resources().registerPattern("org/flywaydb/core/internal/*");
			hints.resources().registerPattern("com/atilika/kuromoji/ipadic/*.bin");
		}
	}

}
