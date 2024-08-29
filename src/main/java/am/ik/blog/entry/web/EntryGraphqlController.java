package am.ik.blog.entry.web;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import am.ik.blog.entry.AuthorizedEntryService;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import graphql.schema.DataFetchingFieldSelectionSet;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;

@Controller
public class EntryGraphqlController {

	private final AuthorizedEntryService entryService;

	public EntryGraphqlController(AuthorizedEntryService entryService) {
		this.entryService = entryService;
	}

	@QueryMapping
	public Optional<Entry> getEntry(@Argument Long entryId, @Argument String tenantId,
			DataFetchingFieldSelectionSet selectionSet) {
		final boolean excludeContent = !selectionSet.contains("content");
		return this.entryService.findOne(entryId, tenantId, excludeContent);
	}

	@QueryMapping
	public EntryConnection getEntries(@Argument Integer first, @Argument Optional<String> after,
			@Argument List<Long> entryIds, @Argument String tenantId, @Argument String query, @Argument String tag,
			@Argument List<String> categories, @Argument String createdBy, @Argument String updatedBy,
			DataFetchingFieldSelectionSet selections) {
		final int pageSize = first == null ? (CollectionUtils.isEmpty(entryIds) ? 20 : entryIds.size()) : first;
		@SuppressWarnings("NullAway")
		final CursorPageRequest<Instant> pageRequest = new CursorPageRequest<>(after.map(Instant::parse).orElse(null),
				pageSize, CursorPageRequest.Navigation.NEXT);
		final SearchCriteria searchCriteria = SearchCriteria.builder()
			.keyword(query) //
			.tag(tag) //
			.stringCategories(categories) //
			.createdBy(createdBy) //
			.lastModifiedBy(updatedBy) //
			.entryIds(entryIds) //
			.excludeEntryId(!selections.contains("edges/node/entryId")) //
			.excludeTitle(!selections.contains("edges/node/frontMatter/title")) //
			.excludeContent(!selections.contains("edges/node/content")) //
			.excludeCategories(!selections.contains("edges/node/frontMatter/categories")) //
			.excludeTags(!selections.contains("edges/node/frontMatter/tags")) //
			.excludeCreatedBy(!selections.contains("edges/node/created/name")) //
			.excludeCreatedBy(!selections.contains("edges/node/created/date")) //
			.excludeLastModifiedBy(!selections.contains("edges/node/updated/name")) //
			.excludeLastModifiedDate(!selections.contains("pageInfo/endCursor")
					&& !selections.contains("pageInfo/startCursor") && !selections.contains("edges/node/updated/date"))
			.build();
		final CursorPage<Entry, Instant> page = this.entryService.findPage(searchCriteria, tenantId, pageRequest);
		final List<EntryEdge> edges = page.content().stream().map(EntryEdge::new).toList();
		return new EntryConnection(edges, new PageInfo(edges, page.hasNext(), page.hasPrevious()));
	}

	public record EntryEdge(Entry node) {
		@JsonProperty
		@Nullable
		public String cursor() {
			if (node.getUpdated() != null && node.getUpdated().date() != null) {
				return DateTimeFormatter.ISO_DATE_TIME.format(node.getUpdated().date());
			}
			return null;
		}
	}

	public record PageInfo(@Nullable String startCursor, @Nullable String endCursor, boolean hasNextPage,
			boolean hadPreviousPage) {
		public PageInfo(List<EntryEdge> edges, boolean hasNextPage, boolean hadPreviousPage) {
			this(edges.isEmpty() ? null : edges.get(0).cursor(),
					edges.isEmpty() ? null : edges.get(edges.size() - 1).cursor(), hasNextPage, hadPreviousPage);
		}
	}

	public record EntryConnection(List<EntryEdge> edges, PageInfo pageInfo) {
	}

}
