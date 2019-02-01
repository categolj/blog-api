package am.ik.blog.entry.v2;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import am.ik.blog.entry.Category;
import am.ik.blog.entry.Content;
import am.ik.blog.entry.EntryId;
import am.ik.blog.entry.EventTime;
import am.ik.blog.entry.Tag;
import am.ik.blog.entry.Title;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class AssertsV2 {

	public static IsContentExcluded assertEntry99999(EntryV2 entry) {
		assertThat(entry).isNotNull();
		assertThat(entry.entryId()).isEqualTo(new EntryId(99999L));
		assertThat(entry.frontMatter().title()).isEqualTo(new Title("Hello World!!"));
		assertThat(entry.frontMatter().tags().collect(toList()))
				.containsExactly(new Tag("test1"), new Tag("test2"), new Tag("test3"));
		assertThat(entry.frontMatter().categories().collect(toList()))
				.containsExactly(new Category("x"), new Category("y"), new Category("z"));
		// premium service is no longer supported
		// assertThat(entry.isPremium()).isFalse();
		return new IsContentExcluded(entry, new Content("This is a test data."),
				new EventTime(
						OffsetDateTime.of(2017, 4, 1, 1, 0, 0, 0, ZoneOffset.ofHours(9))),
				new EventTime(OffsetDateTime.of(2017, 4, 1, 2, 0, 0, 0,
						ZoneOffset.ofHours(9))));
	}

	public static IsContentExcluded assertEntry99998(EntryV2 entry) {
		assertThat(entry).isNotNull();
		assertThat(entry.entryId()).isEqualTo(new EntryId(99998L));
		assertThat(entry.frontMatter().title()).isEqualTo(new Title("Test!!"));
		assertThat(entry.frontMatter().tags().collect(toList()))
				.containsExactly(new Tag("test1"), new Tag("test2"));
		assertThat(entry.frontMatter().categories().collect(toList()))
				.containsExactly(new Category("a"), new Category("b"), new Category("c"));
		// premium service is no longer supported
		// assertThat(entry.isPremium()).isFalse();
		return new IsContentExcluded(entry, new Content("This is a test data."),
				new EventTime(
						OffsetDateTime.of(2017, 4, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))),
				new EventTime(OffsetDateTime.of(2017, 4, 1, 0, 0, 0, 0,
						ZoneOffset.ofHours(9))));
	}

	public static IsContentExcluded assertEntry99997(EntryV2 entry) {
		assertThat(entry).isNotNull();
		assertThat(entry.entryId()).isEqualTo(new EntryId(99997L));
		assertThat(entry.frontMatter().title()).isEqualTo(new Title("CategoLJ 4"));
		assertThat(entry.frontMatter().tags().collect(toList()))
				.containsExactly(new Tag("test1"), new Tag("test3"));
		assertThat(entry.frontMatter().categories().collect(toList()))
				.containsExactly(new Category("x"), new Category("y"));
		// premium service is no longer supported
		// assertThat(entry.isPremium()).isTrue();
		// assertThat(entry.frontMatter.point).isEqualTo(new PremiumPoint(50));
		return new IsContentExcluded(entry, new Content("This is a test data."),
				new EventTime(OffsetDateTime.of(2017, 3, 31, 0, 0, 0, 0,
						ZoneOffset.ofHours(9))),
				new EventTime(OffsetDateTime.of(2017, 3, 31, 0, 0, 0, 0,
						ZoneOffset.ofHours(9))));
	}

	public static class IsContentExcluded {
		final EntryV2 entry;
		final Content content;
		final EventTime created;
		final EventTime updated;

		IsContentExcluded(EntryV2 entry, Content content, EventTime created,
				EventTime updated) {
			this.entry = entry;
			this.content = content;
			this.created = created;
			this.updated = updated;
		}

		public HasFrontMatterDates assertThatContentIsNotSet() {
			assertThat(entry.content()).isEqualTo(new Content(""));
			return new HasFrontMatterDates(entry, created, updated);
		}

		public HasFrontMatterDates assertContent() {
			assertThat(entry.content()).isEqualTo(content);
			return new HasFrontMatterDates(entry, created, updated);
		}

	}

	public static class HasFrontMatterDates {
		final EntryV2 entry;
		final EventTime created;
		final EventTime updated;

		HasFrontMatterDates(EntryV2 entry, EventTime created, EventTime updated) {
			this.entry = entry;
			this.created = created;
			this.updated = updated;
		}

		public void assertFrontMatterDates() {
			assertThat(entry.frontMatter().date()).isEqualTo(created);
			assertThat(entry.frontMatter().updated()).isEqualTo(updated);
		}
	}
}
