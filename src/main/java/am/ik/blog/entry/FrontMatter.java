package am.ik.blog.entry;

import java.util.List;

import am.ik.blog.category.Category;
import am.ik.blog.tag.Tag;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

@JsonDeserialize(builder = FrontMatterBuilder.class)
public record FrontMatter(@NonNull String title, @Nullable List<Category> categories, @Nullable List<Tag> tags) {

	public static Validator<FrontMatter> validator = ValidatorBuilder.<FrontMatter>of()
		.constraint(FrontMatter::title, "title", c -> c.notBlank().asByteArray().lessThanOrEqual(512))
		.constraint(FrontMatter::categories, "categories", c -> c.greaterThanOrEqual(1))
		.build();

	public static final String SEPARATOR = "---";

	@Override
	@NonNull
	public List<Category> categories() {
		if (CollectionUtils.isEmpty(categories)) {
			return List.of();
		}
		return categories;
	}

	@Override
	@NonNull
	public List<Tag> tags() {
		if (CollectionUtils.isEmpty(tags)) {
			return List.of();
		}
		return tags;
	}
}
