package am.ik.blog;

import java.util.LinkedHashSet;

import am.ik.blog.category.CategoryMapper;
import am.ik.blog.entry.EntryMapper;
import am.ik.blog.tag.TagMapper;
import am.ik.github.core.Commit;
import am.ik.github.core.Committer;
import am.ik.github.core.Content;
import am.ik.github.core.ContentType;
import am.ik.github.core.Parent;
import am.ik.github.core.Tree;
import am.ik.github.repositories.commits.CommitsResponse;
import am.ik.github.repositories.contents.ContentsResponse;
import reactor.core.publisher.Hooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.r2dbc.ConnectionFactoryHealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.JavaInfo;
import org.springframework.nativex.hint.AotProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.hint.TypeHint;

import static org.springframework.nativex.hint.TypeAccess.DECLARED_CONSTRUCTORS;
import static org.springframework.nativex.hint.TypeAccess.DECLARED_FIELDS;
import static org.springframework.nativex.hint.TypeAccess.DECLARED_METHODS;
import static org.springframework.nativex.hint.TypeAccess.PUBLIC_CONSTRUCTORS;
import static org.springframework.nativex.hint.TypeAccess.PUBLIC_FIELDS;
import static org.springframework.nativex.hint.TypeAccess.PUBLIC_METHODS;

@SpringBootApplication
@NativeHint(
		options = { "--enable-http" },
		types = {
				@TypeHint(
						typeNames = {
								"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinActiveMqSenderConfiguration",
								"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinRabbitSenderConfiguration",
								"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinKafkaSenderConfiguration",
								"org.springframework.boot.info.JavaInfo",
								"org.springframework.boot.info.JavaInfo$JavaRuntimeEnvironmentInfo.class",
								"org.springframework.boot.info.JavaInfo$JavaVirtualMachineInfo.class"
						},
						types = {
								ConnectionFactoryHealthIndicator.class,
								LinkedHashSet.class,
								CommitsResponse.Commit.class,
								CommitsResponse.Committer.class,
								ContentsResponse.Put.class,
								ContentsResponse.Delete.class,
								ContentsResponse.File.class,
								Commit.class,
								Committer.class,
								Content.class,
								ContentType.class,
								Parent.class,
								Tree.class,
								JavaInfo.class,
								JavaInfo.JavaRuntimeEnvironmentInfo.class,
								JavaInfo.JavaVirtualMachineInfo.class
						},
						access = { DECLARED_FIELDS, DECLARED_METHODS, DECLARED_CONSTRUCTORS, PUBLIC_FIELDS, PUBLIC_METHODS, PUBLIC_CONSTRUCTORS }
				)
		},
		aotProxies = {
				@AotProxyHint(targetClass = EntryMapper.class, proxyFeatures = ProxyBits.IS_STATIC),
				@AotProxyHint(targetClass = TagMapper.class, proxyFeatures = ProxyBits.IS_STATIC),
				@AotProxyHint(targetClass = CategoryMapper.class, proxyFeatures = ProxyBits.IS_STATIC)
		}
)
public class BlogApiApplication {

	public static void main(String[] args) {
		Hooks.onErrorDropped(e -> { /* https://github.com/rsocket/rsocket-java/issues/1018 */});
		SpringApplication.run(BlogApiApplication.class, args);
	}
}
