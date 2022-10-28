package am.ik.blog.github;

import am.ik.blog.entry.Author;
import am.ik.blog.category.Category;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.entry.FrontMatterBuilder;
import am.ik.blog.tag.Tag;

import java.time.OffsetDateTime;
import java.util.List;

public class Fixtures {

    public static Entry entry(Long entryId) {
        OffsetDateTime now = OffsetDateTime.now();
        List<Category> categories = List.of(new Category("foo"), new Category("bar"),
            new Category("hoge"));
        List<Tag> tags = List.of(new Tag("a"), new Tag("b"), new Tag("c"));
        return new EntryBuilder() //
            .withEntryId(entryId) //
            .withContent("Hello") //
            .withFrontMatter(
                new FrontMatterBuilder()
                    .withTitle("Hello")
                    .withCategories(categories)
                    .withTags(tags)
                    .build()) //
            .withCreated(new Author("demo", now)) //
            .withUpdated(new Author("demo", now)) //
            .build();
    }
}