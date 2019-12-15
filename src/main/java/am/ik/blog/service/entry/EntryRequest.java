package am.ik.blog.service.entry;

import am.ik.blog.service.entry.search.CategoryOrders;
import am.ik.blog.service.entry.search.SearchCriteria;
import am.ik.blog.service.entry.search.SearchCriteria.SearchCriteriaBuilder;
import am.ik.blog.model.Tag;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static java.util.Objects.requireNonNullElse;

public class EntryRequest {

    private final SearchCriteriaBuilder builder;

    private final Pageable pageable;

    @JsonCreator
    public EntryRequest(@JsonProperty("query") String query,
                        @JsonProperty("tag") String tag,
                        @JsonProperty("categories") List<String> categories,
                        @JsonProperty("createdBy") String createdBy,
                        @JsonProperty("updatedBy") String updatedBy,
                        @JsonProperty("page") Integer page,
                        @JsonProperty("size") Integer size) {
        this.builder = SearchCriteria.builder()
            .keyword(query)
            .tag((tag == null) ? null : Tag.of(tag))
            .categoryOrders((categories == null) ? null : CategoryOrders.from(categories))
            .createdBy(createdBy)
            .lastModifiedBy(updatedBy);
        this.pageable = PageRequest.of(requireNonNullElse(page, 0), requireNonNullElse(size, 10));
    }

    public SearchCriteria toCriteria(boolean excludeContent) {
        return this.builder.excludeContent(excludeContent).build();
    }

    public SearchCriteria toCriteria() {
        return this.toCriteria(true);
    }

    public Pageable toPageable() {
        return this.pageable;
    }
}
