package am.ik.blog.entry.v2;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Objects;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.Content;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryId;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = EntryV2Serializer.class)
@JsonDeserialize(using = EntryV2Deserializer.class)
public class EntryV2 implements Serializable {
	@JsonUnwrapped
	private final EntryId entryId;
	@JsonUnwrapped
	private final Content content;
	private final Author created;
	private final Author updated;
	private final FrontMatterV2 frontMatter;

	@ConstructorProperties({ "entryId", "content", "created", "updated", "frontMatter" })
	public EntryV2(EntryId entryId, Content content, Author created, Author updated,
			FrontMatterV2 frontMatter) {
		this.entryId = entryId;
		this.content = content;
		this.created = created;
		this.updated = updated;
		this.frontMatter = frontMatter;
	}

	public static EntryV2 from(Entry entry) {
		return new EntryV2(entry.entryId(), entry.content(), entry.getCreated(),
				entry.getUpdated(), FrontMatterV2.from(entry.frontMatter()));
	}

	public static EntryBuilder builder() {
		return new EntryBuilder();
	}

	public EntryBuilder copy() {
		return EntryV2.builder().entryId(entryId).content(content).created(created)
				.updated(updated).frontMatter(frontMatter);
	}

	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof EntryV2)) {
			return false;
		}
		final EntryV2 other = (EntryV2) o;
		if (!other.canEqual((Object) this)) {
			return false;
		}
		final Object this$entryId = this.getEntryId();
		final Object other$entryId = other.getEntryId();
		if (!Objects.equals(this$entryId, other$entryId)) {
			return false;
		}
		final Object this$content = this.getContent();
		final Object other$content = other.getContent();
		if (!Objects.equals(this$content, other$content)) {
			return false;
		}
		final Object this$created = this.getCreated();
		final Object other$created = other.getCreated();
		if (!Objects.equals(this$created, other$created)) {
			return false;
		}
		final Object this$updated = this.getUpdated();
		final Object other$updated = other.getUpdated();
		if (!Objects.equals(this$updated, other$updated)) {
			return false;
		}
		final Object this$frontMatter = this.getFrontMatter();
		final Object other$frontMatter = other.getFrontMatter();
		if (!Objects.equals(this$frontMatter, other$frontMatter)) {
			return false;
		}
		return true;
	}

	@Deprecated
	public Content getContent() {
		return this.content;
	}

	@Deprecated
	public Author getCreated() {
		return this.created;
	}

	@Deprecated
	public EntryId getEntryId() {
		return this.entryId;
	}

	@Deprecated
	public FrontMatterV2 getFrontMatter() {
		return this.frontMatter;
	}

	@Deprecated
	public Author getUpdated() {
		return this.updated;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $entryId = this.getEntryId();
		result = result * PRIME + ($entryId == null ? 43 : $entryId.hashCode());
		final Object $content = this.getContent();
		result = result * PRIME + ($content == null ? 43 : $content.hashCode());
		final Object $created = this.getCreated();
		result = result * PRIME + ($created == null ? 43 : $created.hashCode());
		final Object $updated = this.getUpdated();
		result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
		final Object $frontMatter = this.getFrontMatter();
		result = result * PRIME + ($frontMatter == null ? 43 : $frontMatter.hashCode());
		return result;
	}

	public String toString() {
		return "Entry(entryId=" + this.getEntryId() + ", content=" + this.getContent()
				+ ", created=" + this.getCreated() + ", updated=" + this.getUpdated()
				+ ", frontMatter=" + this.getFrontMatter() + ")";
	}

	public EntryV2 useFrontMatterDate() {
		EntryBuilder builder = copy();
		if (frontMatter != null && frontMatter.date() != null
				&& frontMatter.date().getValue() != null && created != null) {
			builder.created(new Author(created.getName(), frontMatter.date()));
		}
		if (frontMatter != null && frontMatter.updated() != null
				&& frontMatter.updated().getValue() != null && updated != null) {
			builder.updated(new Author(updated.getName(), frontMatter.updated()));
		}
		return builder.build();
	}

	public static boolean isPublicFileName(String fileName) {
		return fileName.matches("[0-9]+\\.md");
	}

	public EntryId entryId() {
		return entryId;
	}

	public Content content() {
		return content;
	}

	public Author created() {
		return this.created;
	}

	public Author updated() {
		return this.updated;
	}

	public FrontMatterV2 frontMatter() {
		return frontMatter;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof EntryV2;
	}

	public static class EntryBuilder {

		private Content content;

		private Author created;

		private EntryId entryId;

		private FrontMatterV2 frontMatter;

		private Author updated;

		EntryBuilder() {
		}

		public EntryV2 build() {
			return new EntryV2(entryId, content, created, updated, frontMatter);
		}

		public EntryBuilder content(Content content) {
			this.content = content;
			return this;
		}

		public EntryBuilder created(Author created) {
			this.created = created;
			return this;
		}

		public EntryBuilder entryId(EntryId entryId) {
			this.entryId = entryId;
			return this;
		}

		public EntryBuilder frontMatter(FrontMatterV2 frontMatter) {
			this.frontMatter = frontMatter;
			return this;
		}

		public String toString() {
			return "Entry.EntryBuilder(entryId=" + this.entryId + ", content="
					+ this.content + ", created=" + this.created + ", updated="
					+ this.updated + ", frontMatter=" + this.frontMatter + ")";
		}

		public EntryBuilder updated(Author updated) {
			this.updated = updated;
			return this;
		}
	}
}
