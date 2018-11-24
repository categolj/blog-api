package am.ik.blog.entry;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class Asserts {

	public static IsContentExcluded assertEntry99999(Entry entry) {
		assertThat(entry).isNotNull();
		assertThat(entry.entryId).isEqualTo(new EntryId(99999L));
		assertThat(entry.frontMatter.title).isEqualTo(new Title("Hello World!!"));
		assertThat(entry.frontMatter.tags.collect(toList()))
				.containsExactly(new Tag("test1"), new Tag("test2"), new Tag("test3"));
		assertThat(entry.frontMatter.categories.collect(toList()))
				.containsExactly(new Category("x"), new Category("y"), new Category("z"));
		// premium service is no longer supported
		// assertThat(entry.isPremium()).isFalse();
		return new IsContentExcluded(entry, new Content("This is a test data."),
				new EventTime(
						OffsetDateTime.of(2017, 4, 1, 1, 0, 0, 0, ZoneOffset.ofHours(9))),
				new EventTime(OffsetDateTime.of(2017, 4, 1, 2, 0, 0, 0,
						ZoneOffset.ofHours(9))));
	}

	public static IsContentExcluded assertEntry99998(Entry entry) {
		assertThat(entry).isNotNull();
		assertThat(entry.entryId).isEqualTo(new EntryId(99998L));
		assertThat(entry.frontMatter.title).isEqualTo(new Title("Test!!"));
		assertThat(entry.frontMatter.tags.collect(toList()))
				.containsExactly(new Tag("test1"), new Tag("test2"));
		assertThat(entry.frontMatter.categories.collect(toList()))
				.containsExactly(new Category("a"), new Category("b"), new Category("c"));
		// premium service is no longer supported
		// assertThat(entry.isPremium()).isFalse();
		return new IsContentExcluded(entry, new Content("This is a test data."),
				new EventTime(
						OffsetDateTime.of(2017, 4, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9))),
				new EventTime(OffsetDateTime.of(2017, 4, 1, 0, 0, 0, 0,
						ZoneOffset.ofHours(9))));
	}

	public static IsContentExcluded assertEntry99997(Entry entry) {
		assertThat(entry).isNotNull();
		assertThat(entry.entryId).isEqualTo(new EntryId(99997L));
		assertThat(entry.frontMatter.title).isEqualTo(new Title("CategoLJ 4"));
		assertThat(entry.frontMatter.tags.collect(toList()))
				.containsExactly(new Tag("test1"), new Tag("test3"));
		assertThat(entry.frontMatter.categories.collect(toList()))
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
		final Entry entry;
		final Content content;
		final EventTime created;
		final EventTime updated;

		IsContentExcluded(Entry entry, Content content, EventTime created,
				EventTime updated) {
			this.entry = entry;
			this.content = content;
			this.created = created;
			this.updated = updated;
		}

		public HasFrontMatterDates assertThatContentIsNotSet() {
			assertThat(entry.content).isEqualTo(new Content(""));
			return new HasFrontMatterDates(entry, created, updated);
		}

		public HasFrontMatterDates assertContent() {
			assertThat(entry.content).isEqualTo(content);
			return new HasFrontMatterDates(entry, created, updated);
		}

	}

	public static class HasFrontMatterDates {
		final Entry entry;
		final EventTime created;
		final EventTime updated;

		HasFrontMatterDates(Entry entry, EventTime created, EventTime updated) {
			this.entry = entry;
			this.created = created;
			this.updated = updated;
		}

		public void assertFrontMatterDates() {
			assertThat(entry.frontMatter.date).isEqualTo(created);
			assertThat(entry.frontMatter.updated).isEqualTo(updated);
		}
	}
}
