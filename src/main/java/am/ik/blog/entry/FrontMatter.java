package am.ik.blog.entry;

import am.ik.blog.category.Category;
import am.ik.blog.tag.Tag;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.StringJoiner;

@JsonDeserialize(builder = FrontMatterBuilder.class)
public class FrontMatter {
	public static Validator<FrontMatter> validator = ValidatorBuilder.<FrontMatter>of()
			.constraint(FrontMatter::getTitle, "title", c -> c.notBlank().asByteArray().lessThanOrEqual(512))
			.constraint(FrontMatter::getCategories, "categories", c -> c.greaterThanOrEqual(1))
			.build();

	public static final String SEPARATOR = "---";

	private final String title;

	private final List<Category> categories;

	private final List<Tag> tags;

	public FrontMatter(String title, List<Category> categories, List<Tag> tags) {
		this.title = title;
		this.categories = categories;
		this.tags = tags;
	}

	public String getTitle() {
		return title;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public List<Tag> getTags() {
		return tags;
	}

	@Override
	public String toString() {
		return "FrontMatter{" +
				"title='" + title + '\'' +
				", categories=" + categories +
				", tags=" + tags +
				'}';
	}
}
