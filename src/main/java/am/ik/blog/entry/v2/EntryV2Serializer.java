package am.ik.blog.entry.v2;

import java.io.IOException;

import am.ik.blog.entry.Author;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.springframework.boot.jackson.JsonObjectSerializer;

public class EntryV2Serializer extends JsonObjectSerializer<EntryV2> {

	@Override
	protected void serializeObject(EntryV2 entry, JsonGenerator jsonGenerator,
			SerializerProvider provider) throws IOException {
		jsonGenerator.writeNumberField("entryId", entry.entryId().getValue());
		jsonGenerator.writeStringField("content", entry.content().getValue());
		this.writeAuthor(jsonGenerator, "created", entry.created());
		this.writeAuthor(jsonGenerator, "updated", entry.updated());
		jsonGenerator.writeObjectField("frontMatter", entry.frontMatter());

	}

	private void writeAuthor(JsonGenerator jsonGenerator, String name, Author author)
			throws IOException {
		jsonGenerator.writeFieldName(name);
		jsonGenerator.writeStartObject();
		jsonGenerator.writeFieldName("name");
		jsonGenerator.writeString(author.getName().getValue());
		jsonGenerator.writeFieldName("date");
		jsonGenerator.writeObject(author.getDate().getValue());
		jsonGenerator.writeEndObject();
	}

}
