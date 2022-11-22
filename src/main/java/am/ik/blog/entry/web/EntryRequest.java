package am.ik.blog.entry.web;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.FrontMatter;

public record EntryRequest(String content, FrontMatter frontMatter, Author created,
						   Author updated) {

	public Author createdOrNullAuthor() {
		if (created != null) {
			return created;
		}
		return Author.NULL_AUTHOR;
	}

	public Author updatedOrNullAuthor() {
		if (updated != null) {
			return updated;
		}
		return Author.NULL_AUTHOR;
	}
}
