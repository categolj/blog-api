package am.ik.blog.entry.web;

import java.util.List;

import am.ik.blog.entry.search.CategoryOrders;
import am.ik.blog.entry.search.SearchCriteria;
import am.ik.blog.entry.search.SearchCriteria.SearchCriteriaBuilder;
import am.ik.blog.tag.Tag;

public class EntrySearchRequest {
	private String query;

	private String tag;

	private List<String> categories;

	private String createdBy;

	private String updatedBy;

	private boolean excludeContent = true;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public boolean isExcludeContent() {
		return excludeContent;
	}

	public void setExcludeContent(boolean excludeContent) {
		this.excludeContent = excludeContent;
	}

	public SearchCriteria toCriteria() {
		final SearchCriteriaBuilder builder = SearchCriteria.builder()
				.keyword(query)
				.tag((tag == null) ? null : new Tag(tag))
				.categoryOrders((categories == null) ? null : CategoryOrders.from(categories))
				.createdBy(createdBy)
				.lastModifiedBy(updatedBy);
		return builder.excludeContent(excludeContent).build();
	}
}
