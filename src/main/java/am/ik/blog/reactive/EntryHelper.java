package am.ik.blog.reactive;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryId;
import am.ik.blog.entry.EntryMapper;

import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Component;

@Component
public class EntryHelper {
	private final EntryMapper entryMapper;

	public EntryHelper(EntryMapper entryMapper) {
		this.entryMapper = entryMapper;
	}

	@NewSpan
	public Entry getEntry(@SpanTag("entryId") EntryId entryId,
			@SpanTag("excludeContent") boolean excludeContent) {
		return this.entryMapper.findOne(entryId, excludeContent);
	}
}
