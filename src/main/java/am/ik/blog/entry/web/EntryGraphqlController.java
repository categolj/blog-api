package am.ik.blog.entry.web;

import java.util.Optional;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryService;
import graphql.schema.DataFetchingFieldSelectionSet;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class EntryGraphqlController {
	private final EntryService entryService;

	public EntryGraphqlController(EntryService entryService) {
		this.entryService = entryService;
	}

	@QueryMapping
	public Optional<Entry> getEntry(@Argument Long entryId,
			DataFetchingFieldSelectionSet selectionSet) {
		return this.getEntryForTenant(entryId, null, selectionSet);
	}

	@QueryMapping
	public Optional<Entry> getEntryForTenant(@Argument Long entryId,
			@Argument String tenantId, DataFetchingFieldSelectionSet selectionSet) {
		final boolean excludeContent = !selectionSet.contains("content");
		return this.entryService.findOne(entryId, tenantId, excludeContent);
	}
}
