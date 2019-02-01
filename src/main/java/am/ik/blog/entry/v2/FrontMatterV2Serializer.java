package am.ik.blog.entry.v2;

import java.io.IOException;
import java.io.UncheckedIOException;

import am.ik.blog.entry.Categories;
import am.ik.blog.entry.Tags;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.springframework.boot.jackson.JsonObjectSerializer;

public class FrontMatterV2Serializer extends JsonObjectSerializer<FrontMatterV2> {

	@Override
	protected void serializeObject(FrontMatterV2 frontMatter, JsonGenerator jsonGenerator,
			SerializerProvider provider) throws IOException {
		jsonGenerator.writeStringField("title", frontMatter.title().getValue());
		Tags tags = frontMatter.tags();
		if (tags != null) {
			jsonGenerator.writeArrayFieldStart("tags");
			tags.stream().forEach(tag -> {
				try {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeFieldName("tag");
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("name", tag.toString());
					jsonGenerator.writeEndObject();
					jsonGenerator.writeEndObject();
				}
				catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			jsonGenerator.writeEndArray();
		}
		Categories categories = frontMatter.categories();
		if (categories != null) {
			jsonGenerator.writeArrayFieldStart("categories");
			categories.stream().forEach(category -> {
				try {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("name", category.toString());
					jsonGenerator.writeEndObject();
				}
				catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			jsonGenerator.writeEndArray();
		}
	}
}
