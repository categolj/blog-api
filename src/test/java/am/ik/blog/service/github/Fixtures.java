package am.ik.blog.service.github;

import am.ik.blog.model.Author;
import am.ik.blog.model.Category;
import am.ik.blog.model.Entry;
import am.ik.blog.model.EntryBuilder;
import am.ik.blog.model.FrontMatterBuilder;
import am.ik.blog.model.Tag;

import java.time.OffsetDateTime;
import java.util.List;

public class Fixtures {

    public static Entry entry(Long entryId) {
        OffsetDateTime now = OffsetDateTime.now();
        List<Category> categories = List.of(Category.of("foo"), Category.of("bar"),
            Category.of("hoge"));
        List<Tag> tags = List.of(Tag.of("a"), Tag.of("b"), Tag.of("c"));
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