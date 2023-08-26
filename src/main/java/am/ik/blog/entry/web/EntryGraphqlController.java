package am.ik.blog.entry.web;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import am.ik.blog.category.Category;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryService;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.tag.Tag;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
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
	public Optional<Entry> getEntry(@Argument Long entryId, @Argument String tenantId,
			DataFetchingFieldSelectionSet selectionSet) {
		final boolean excludeContent = !selectionSet.contains("content");
		return this.entryService.findOne(entryId, tenantId, excludeContent);
	}

	@QueryMapping
	public EntryConnection getEntries(@Argument Integer first,
			@Argument Optional<String> after, @Argument String tenantId,
			@Argument String query, @Argument String tag,
			@Argument List<String> categories, @Argument String createdBy,
			@Argument String updatedBy, DataFetchingFieldSelectionSet selectionSet) {
		final CursorPageRequest<Instant> pageRequest = new CursorPageRequest<>(
				after.map(Instant::parse).orElse(null), first,
				CursorPageRequest.Navigation.NEXT);
		final boolean excludeContent = !selectionSet.contains("edges/node/content");
		final SearchCriteria searchCriteria = SearchCriteria.builder().keyword(query)
				.tag((tag == null) ? null : new Tag(tag))
				.categories((categories == null) ? List.of()
						: categories.stream().map(Category::new).toList())
				.createdBy(createdBy).lastModifiedBy(updatedBy)
				.excludeContent(excludeContent).build();

		final CursorPage<Entry, Instant> page = this.entryService.findPage(searchCriteria,
				tenantId, pageRequest);

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
