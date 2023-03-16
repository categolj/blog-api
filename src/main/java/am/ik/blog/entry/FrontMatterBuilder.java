package am.ik.blog.entry;

import am.ik.blog.category.Category;
import am.ik.blog.tag.Tag;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.yaml.snakeyaml.Yaml;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@JsonPOJOBuilder
public class FrontMatterBuilder {

	private List<Category> categories;

	private List<Tag> tags;

	private String title;

	public FrontMatter build() {
		return new FrontMatter(title, categories, tags);
	}

	public FrontMatterBuilder withCategories(List<Category> categories) {
		this.categories = categories;
		return this;
	}

	public FrontMatterBuilder withTags(List<Tag> tags) {
		this.tags = tags;
		return this;
	}

	public FrontMatterBuilder withTitle(String title) {
		this.title = title;
		return this;
	}

	@SuppressWarnings("unchecked")
	public static Tuple3<FrontMatter, Optional<OffsetDateTime>, Optional<OffsetDateTime>> parseYaml(
			String string) {
		final Yaml yaml = new Yaml();
		Map<String, Object> map = yaml.load(string);
		if (map == null) {
			map = new LinkedHashMap<>(); // avoid null pointer exception
		}
		final FrontMatter frontMatter = new FrontMatterBuilder()
				.withTitle((String) map.getOrDefault("title", "no title"))
				.withCategories(((List<String>) map.computeIfAbsent("categories",
						key -> emptyList())).stream().map(Category::new)
						.collect(toList()))
				.withTags(((List<String>) map.computeIfAbsent("tags", key -> emptyList()))
						.stream().map(Tag::new).collect(toList()))
				.build();
		final OffsetDateTime date = map.containsKey("date")
				? OffsetDateTime.ofInstant(((Date) map.get("date")).toInstant(),
						ZoneId.of("UTC"))
				: null;
		final OffsetDateTime updated = map.containsKey("updated")
				? OffsetDateTime.ofInstant(((Date) map.get("updated")).toInstant(),
						ZoneId.of("UTC"))
				: null;
		return Tuples.of(frontMatter, Optional.ofNullable(date),
				Optional.ofNullable(updated));
	}
}