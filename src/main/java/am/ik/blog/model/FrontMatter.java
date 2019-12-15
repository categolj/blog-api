package am.ik.blog.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.StringJoiner;

@JsonDeserialize(builder = FrontMatterBuilder.class)
public class FrontMatter {

    public static final String SEPARATOR = "---";

    private final String title;

    private final List<Category> categories;

    private final List<Tag> tags;

    FrontMatter(String title, List<Category> categories, List<Tag> tags) {
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
        return new StringJoiner(", ", FrontMatter.class.getSimpleName() + "[", "]")
            .add("title='" + title + "'").add("categories=" + categories)
            .add("tags=" + tags).toString();
    }
}
