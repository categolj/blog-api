package am.ik.blog.entry.v2;

import java.io.IOException;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.Content;
import am.ik.blog.entry.EntryId;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.boot.jackson.JsonObjectDeserializer;

public class EntryV2Deserializer extends JsonObjectDeserializer<EntryV2> {

	@Override
	protected EntryV2 deserializeObject(JsonParser jsonParser,
			DeserializationContext deserializationContext, ObjectCodec objectCodec,
			JsonNode jsonNode) throws IOException {
		EntryV2.EntryBuilder builder = EntryV2.builder();
		builder.entryId(new EntryId(nullSafeValue(jsonNode.get("entryId"), Long.class)));
		builder.content(
				new Content(nullSafeValue(jsonNode.get("content"), String.class)));
		JsonNode frontMatterNode = jsonNode.get("frontMatter");
		FrontMatterV2 frontMatter = objectCodec
				.readValue(frontMatterNode.traverse(objectCodec), FrontMatterV2.class);
		builder.frontMatter(frontMatter);
		JsonNode createdNode = jsonNode.get("created");
		Author created = objectCodec.readValue(createdNode.traverse(objectCodec),
				Author.class);
		builder.created(created);
		JsonNode updatedNode = jsonNode.get("updated");
		Author updated = objectCodec.readValue(updatedNode.traverse(objectCodec),
				Author.class);
		builder.updated(updated);
		return builder.build();
	}
}
