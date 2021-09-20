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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.AotProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.hint.TypeHint;

@SpringBootApplication
@NativeHint(
		options = { "--enable-http" },
		types = {
				@TypeHint(
						typeNames = {
								"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinActiveMqSenderConfiguration",
								"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinRabbitSenderConfiguration",
								"org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinKafkaSenderConfiguration"
						},
						types = {
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
								Tree.class
						})
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
