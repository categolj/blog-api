package am.ik.blog.entry.web;

import am.ik.blog.category.Category;
import am.ik.blog.entry.Entry;
import am.ik.blog.tag.Tag;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class Asserts {

    public static IsContentExcluded assertEntry99999(Entry entry) {
        assertThat(entry).isNotNull();
        assertThat(entry.getEntryId()).isEqualTo(99999L);
        assertThat(entry.getFrontMatter().getTitle()).isEqualTo("Hello World!!");
        assertThat(entry.getFrontMatter().getTags())
            .containsExactly(new Tag("test1"), new Tag("test2"), new Tag("test3"));
        assertThat(entry.getFrontMatter().getCategories())
            .containsExactly(new Category("x"), new Category("y"), new Category("z"));
        return new IsContentExcluded(entry, "Hello!",
            OffsetDateTime.of(2017, 4, 1, 1, 0, 0, 0, ZoneOffset.ofHours(9)),
            OffsetDateTime.of(2017, 4, 1, 2, 0, 0, 0,
                ZoneOffset.ofHours(9)));
    }

    public static IsContentExcluded assertEntry99998(Entry entry) {
        assertThat(entry).isNotNull();
        assertThat(entry.getEntryId()).isEqualTo(99998L);
        assertThat(entry.getFrontMatter().getTitle()).isEqualTo("Test!!");
        assertThat(entry.getFrontMatter().getTags())
            .containsExactly(new Tag("test1"), new Tag("test2"));
        assertThat(entry.getFrontMatter().getCategories())
            .containsExactly(new Category("a"), new Category("b"), new Category("c"));
        return new IsContentExcluded(entry, "This is a test data.",
            OffsetDateTime.of(2017, 4, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)),
            OffsetDateTime.of(2017, 4, 1, 0, 0, 0, 0,
                ZoneOffset.ofHours(9)));
    }

    public static IsContentExcluded assertEntry99997(Entry entry) {
        assertThat(entry).isNotNull();
        assertThat(entry.getEntryId()).isEqualTo(99997L);
        assertThat(entry.getFrontMatter().getTitle()).isEqualTo("CategoLJ 4");
        assertThat(entry.getFrontMatter().getTags())
            .containsExactly(new Tag("test1"), new Tag("test3"));
        assertThat(entry.getFrontMatter().getCategories())
            .containsExactly(new Category("x"), new Category("y"));
        return new IsContentExcluded(entry, "This is a test data.",
            OffsetDateTime.of(2017, 3, 31, 0, 0, 0, 0,
                ZoneOffset.ofHours(9)),
            OffsetDateTime.of(2017, 3, 31, 0, 0, 0, 0,
                ZoneOffset.ofHours(9)));
    }

    public static class IsContentExcluded {

        final Entry entry;

        final String content;

        final OffsetDateTime created;

        final OffsetDateTime updated;

        IsContentExcluded(Entry entry, String content, OffsetDateTime created,
                          OffsetDateTime updated) {
            this.entry = entry;
            this.content = content;
            this.created = created;
            this.updated = updated;
        }

        public HasFrontMatterDates assertThatContentIsNotSet() {
            assertThat(entry.getContent()).isEqualTo("");
            return new HasFrontMatterDates(entry, created, updated);
        }

        public HasFrontMatterDates assertContent() {
            assertThat(entry.getContent()).isEqualTo(content);
            return new HasFrontMatterDates(entry, created, updated);
        }

    }

    public static class HasFrontMatterDates {

        final Entry entry;

        final OffsetDateTime created;

        final OffsetDateTime updated;

        HasFrontMatterDates(Entry entry, OffsetDateTime created, OffsetDateTime updated) {
            this.entry = entry;
            this.created = created;
            this.updated = updated;
        }

        public void assertFrontMatterDates() {
            // TODO
            //  assertThat(entry.getFrontMatter().date()).isEqualTo(created);
            //  assertThat(entry.getFrontMatter().updated()).isEqualTo(updated);
        }
    }
}
