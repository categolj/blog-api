package am.ik.blog.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import am.ik.blog.config.NativeHints.RuntimeHints;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.entry.FrontMatterBuilder;
import am.ik.blog.tenant.TenantUserProps;
import jakarta.annotation.Nullable;

import org.apache.tomcat.util.modeler.modules.MbeansDescriptorsIntrospectionSource;
import org.flywaydb.core.internal.configuration.extensions.DeployScriptFilenameConfigurationExtension;
import org.flywaydb.core.internal.configuration.extensions.PrepareScriptFilenameConfigurationExtension;
import org.flywaydb.core.internal.logging.slf4j.Slf4jLogCreator;
import org.flywaydb.core.internal.publishing.PublishingConfigurationExtension;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.util.ReflectionUtils;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(RuntimeHints.class)
public class NativeHints {

	public static class RuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(org.springframework.aot.hint.RuntimeHints hints, @Nullable ClassLoader classLoader) {
			try {
				final List<Method> builderMethods = new ArrayList<>();
				builderMethods.addAll(Arrays.asList(EntryBuilder.class.getDeclaredMethods()));
				builderMethods.addAll(Arrays.asList(FrontMatterBuilder.class.getDeclaredMethods()));
				builderMethods
					.addAll(Arrays.asList(DeployScriptFilenameConfigurationExtension.class.getDeclaredMethods()));
				builderMethods
					.addAll(Arrays.asList(PrepareScriptFilenameConfigurationExtension.class.getDeclaredMethods()));
				builderMethods.addAll(Arrays.asList(PublishingConfigurationExtension.class.getDeclaredMethods()));
				builderMethods.forEach(method -> hints.reflection().registerMethod(method, ExecutableMode.INVOKE));
				hints.reflection().registerConstructor(Slf4jLogCreator.class.getConstructor(), ExecutableMode.INVOKE);
				hints.reflection()
					.registerConstructor(MbeansDescriptorsIntrospectionSource.class.getConstructor(),
							ExecutableMode.INVOKE);
				hints.reflection()
					.registerMethod(Objects.requireNonNull(ReflectionUtils.findMethod(TenantUserProps.class, "users")),
							ExecutableMode.INVOKE);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			hints.resources().registerPattern("org/flywaydb/core/internal/*");
		}

	}

}
