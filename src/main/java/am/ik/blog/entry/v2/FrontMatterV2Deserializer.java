package am.ik.blog.entry.v2;

import java.io.IOException;

import am.ik.blog.entry.Categories;
import am.ik.blog.entry.Category;
import am.ik.blog.entry.EventTime;
import am.ik.blog.entry.Tag;
import am.ik.blog.entry.Tags;
import am.ik.blog.entry.Title;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.boot.jackson.JsonObjectDeserializer;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class FrontMatterV2Deserializer extends JsonObjectDeserializer<FrontMatterV2> {

	@Override
	protected FrontMatterV2 deserializeObject(JsonParser jsonParser,
			DeserializationContext deserializationContext, ObjectCodec objectCodec,
			JsonNode jsonNode) throws IOException {
		Title title = new Title(nullSafeValue(jsonNode.get("title"), String.class));
		JsonNode tagsNode = jsonNode.get("tags");
		Tags tags = tagsNode == null ? new Tags()
				: new Tags(stream(tagsNode.spliterator(), false)
						.map(n -> new Tag(n.get("name").asText())).collect(toList()));
		JsonNode categoriesNode = jsonNode.get("categories");
		Categories categories = categoriesNode == null ? new Categories()
				: new Categories(stream(categoriesNode.spliterator(), false)
						.map(n -> new Category(n.get("name").asText()))
						.collect(toList()));
		return new FrontMatterV2(title, categories, tags, EventTime.UNSET,
				EventTime.UNSET);
	}
}