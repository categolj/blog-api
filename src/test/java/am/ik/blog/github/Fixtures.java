package am.ik.blog.github;

import am.ik.blog.entry.*;

public class Fixtures {

	public static Entry entry(EntryId entryId) {
		EventTime now = EventTime.now();
		Categories categories = new Categories(new Category("foo"), new Category("bar"),
				new Category("hoge"));
		Tags tags = new Tags(new Tag("a"), new Tag("b"), new Tag("c"));
		return Entry.builder() //
				.entryId(entryId) //
				.content(new Content("Hello")) //
				.frontMatter(
						new FrontMatter(new Title("Hello"), categories, tags, now, now)) //
				.created(new Author(new Name("demo"), now)) //
				.updated(new Author(new Name("demo"), now)) //
				.build();
	}
}