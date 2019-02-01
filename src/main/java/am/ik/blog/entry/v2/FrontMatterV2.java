package am.ik.blog.entry.v2;

import java.io.Serializable;
import java.util.Objects;

import am.ik.blog.entry.Categories;
import am.ik.blog.entry.EventTime;
import am.ik.blog.entry.FrontMatter;
import am.ik.blog.entry.Tags;
import am.ik.blog.entry.Title;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = FrontMatterV2Serializer.class)
@JsonDeserialize(using = FrontMatterV2Deserializer.class)
public class FrontMatterV2 implements Serializable {
	@JsonUnwrapped
	private final Title title;
	@JsonUnwrapped
	private final Categories categories;
	@JsonUnwrapped
	private final Tags tags;
	@JsonIgnore
	private final EventTime date;
	@JsonIgnore
	private final EventTime updated;

	public static final String SEPARATOR = "---";

	public FrontMatterV2(Title title, Categories categories, Tags tags, EventTime date,
			EventTime updated) {
		this.title = title;
		this.categories = categories;
		this.tags = tags;
		this.date = defaultValue(date, EventTime.UNSET);
		this.updated = defaultValue(updated, EventTime.UNSET);
	}

	public static FrontMatterV2 from(FrontMatter frontMatter) {
		return new FrontMatterV2(frontMatter.title(), frontMatter.categories(),
				frontMatter.tags(), frontMatter.date(), frontMatter.updated());
	}

	public FrontMatterV2(Title title, Categories categories, Tags tags) {
		this(title, categories, tags, EventTime.UNSET, EventTime.UNSET);
	}

	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof FrontMatterV2)) {
			return false;
		}
		final FrontMatterV2 other = (FrontMatterV2) o;
		if (!other.canEqual((Object) this)) {
			return false;
		}
		final Object this$title = this.getTitle();
		final Object other$title = other.getTitle();
		if (!Objects.equals(this$title, other$title)) {
			return false;
		}
		final Object this$categories = this.getCategories();
		final Object other$categories = other.getCategories();
		if (!Objects.equals(this$categories, other$categories)) {
			return false;
		}
		final Object this$tags = this.getTags();
		final Object other$tags = other.getTags();
		if (!Objects.equals(this$tags, other$tags)) {
			return false;
		}
		final Object this$date = this.getDate();
		final Object other$date = other.getDate();
		if (!Objects.equals(this$date, other$date)) {
			return false;
		}
		final Object this$updated = this.getUpdated();
		final Object other$updated = other.getUpdated();
		if (!Objects.equals(this$updated, other$updated)) {
			return false;
		}
		return true;
	}

	@Deprecated
	public Categories getCategories() {
		return this.categories;
	}

	@Deprecated
	public EventTime getDate() {
		return this.date;
	}

	@Deprecated
	public Tags getTags() {
		return this.tags;
	}

	@Deprecated
	public Title getTitle() {
		return this.title;
	}

	@Deprecated
	public EventTime getUpdated() {
		return this.updated;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $title = this.getTitle();
		result = result * PRIME + ($title == null ? 43 : $title.hashCode());
		final Object $categories = this.getCategories();
		result = result * PRIME + ($categories == null ? 43 : $categories.hashCode());
		final Object $tags = this.getTags();
		result = result * PRIME + ($tags == null ? 43 : $tags.hashCode());
		final Object $date = this.getDate();
		result = result * PRIME + ($date == null ? 43 : $date.hashCode());
		final Object $updated = this.getUpdated();
		result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
		return result;
	}

	public String toString() {
		return "FrontMatter(title=" + this.getTitle() + ", categories="
				+ this.getCategories() + ", tags=" + this.getTags() + ", date="
				+ this.getDate() + ", updated=" + this.getUpdated() + ")";
	}

	protected boolean canEqual(final Object other) {
		return other instanceof FrontMatterV2;
	}

	private <T> T defaultValue(T value, T defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public Title title() {
		return title;
	}

	public Tags tags() {
		return tags;
	}

	public Categories categories() {
		return categories;
	}

	public EventTime date() {
		return date;
	}

	public EventTime updated() {
		return updated;
	}
}
