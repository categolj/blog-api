package am.ik.blog.entry.web;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryService;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import graphql.schema.DataFetchingFieldSelectionSet;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

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

	@QueryMapping
	public EntryConnection getEntries(@Argument Integer first, @Argument String after) {
		final CursorPageRequest<Instant> pageRequest = new CursorPageRequest<>(
				StringUtils.hasText(after) ? Instant.parse(after) : null, first,
				CursorPageRequest.Navigation.NEXT);
		final CursorPage<Entry, Instant> page = this.entryService
				.findPage(SearchCriteria.DEFAULT, null, pageRequest);
		final List<EntryEdge> edges = page.content().stream().map(EntryEdge::new)
				.toList();
		PageInfo pageInfo;
		if (edges.isEmpty()) {
			pageInfo = new PageInfo(null, null, page.hasNext(), page.hasPrevious());
		}
		else {
			pageInfo = new PageInfo(edges.get(0).cursor(),
					edges.get(edges.size() - 1).cursor(), page.hasNext(),
					page.hasPrevious());
		}
		return new EntryConnection(edges, pageInfo);
	}

	public record EntryEdge(Entry node) {
		@JsonProperty
		public String cursor() {
			return node.getUpdated().getDate().toString();
		}
	}

	public record PageInfo(String startCursor, String endCursor, boolean hasNextPage,
			boolean hadPreviousPage) {

	}

	public record EntryConnection(List<EntryEdge> edges, PageInfo pageInfo) {
	}
}
