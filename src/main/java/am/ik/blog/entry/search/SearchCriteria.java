package am.ik.blog.entry.search;

import am.ik.blog.tag.Tag;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchCriteria {

	public static final SearchCriteria DEFAULT = defaults().build();

	public static final String SERIES = "Series";

	private boolean excludeContent;

	private String createdBy;

	private String lastModifiedBy;

	private Tag tag;

	private CategoryOrders categoryOrders;

	private String keyword;

	SearchCriteria(boolean excludeContent, String createdBy, String lastModifiedBy,
			Tag tag, CategoryOrders categoryOrders, String keyword) {
		this.excludeContent = excludeContent;
		this.createdBy = createdBy;
		this.lastModifiedBy = lastModifiedBy;
		this.tag = tag;
		this.categoryOrders = categoryOrders;
		this.keyword = keyword;
	}

	public static SearchCriteriaBuilder builder() {
		return new SearchCriteriaBuilder();
	}

	public static SearchCriteria.SearchCriteriaBuilder defaults() {
		return SearchCriteria.builder().excludeContent();
	}

	public CategoryOrders getCategoryOrders() {
		return this.categoryOrders;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public String getKeyword() {
		return this.keyword;
	}

	public String getLastModifiedBy() {
		return this.lastModifiedBy;
	}

	public Tag getTag() {
		return this.tag;
	}

	public boolean isExcludeContent() {
		return this.excludeContent;
	}

	boolean isExcludeSeries() {
		return this.tag == null && this.categoryOrders == null && !this.hasKeyword();
	}

	boolean hasKeyword() {
		return StringUtils.hasText(this.keyword);
	}

	public MapSqlParameterSource toParameterSource() {
		final MapSqlParameterSource params = new MapSqlParameterSource();
		if (this.hasKeyword()) {
			params.addValue("keyword", this.keyword);
		}
		if (this.createdBy != null) {
			params.addValue("createdBy", this.createdBy);
		}
		if (this.lastModifiedBy != null) {
			params.addValue("lastModifiedBy", this.createdBy);
		}
		if (this.tag != null) {
			params.addValue("tag", tag.name());
		}
		if (this.categoryOrders != null) {
			final List<CategoryOrder> value = this.categoryOrders.getValue().stream()
					.toList();
			params.addValue("categoryOrders", value);
			for (int i = 0; i < value.size(); i++) {
				final CategoryOrder categoryOrder = value.get(i);
				params.addValue("categoryOrder_0_%d.category.name".formatted(i),
						categoryOrder.getCategory().name());
				params.addValue("categoryOrder_0_%d.categoryOrder".formatted(i),
						categoryOrder.getCategoryOrder());
			}
		}
		return params;
	}

	public static class SearchCriteriaBuilder {

		private CategoryOrders categoryOrders;

		private String createdBy;

		private boolean excludeContent;

		private String keyword;

		private String lastModifiedBy;

		private Tag tag;

		SearchCriteriaBuilder() {
		}

		public SearchCriteria build() {
			return new SearchCriteria(excludeContent, createdBy, lastModifiedBy, tag,
					categoryOrders, keyword);
		}

		public SearchCriteriaBuilder categoryOrders(CategoryOrders categoryOrders) {
			this.categoryOrders = categoryOrders;
			return this;
		}

		public SearchCriteriaBuilder createdBy(String createdBy) {
			this.createdBy = createdBy;
			return this;
		}

		public SearchCriteriaBuilder excludeContent(boolean excludeContent) {
			this.excludeContent = excludeContent;
			return this;
		}

		public SearchCriteriaBuilder excludeContent() {
			return this.excludeContent(true);
		}

		public SearchCriteriaBuilder includeContent() {
			return this.excludeContent(false);
		}

		public SearchCriteriaBuilder keyword(String keyword) {
			this.keyword = keyword;
			return this;
		}

		public SearchCriteriaBuilder lastModifiedBy(String lastModifiedBy) {
			this.lastModifiedBy = lastModifiedBy;
			return this;
		}

		public SearchCriteriaBuilder tag(Tag tag) {
			this.tag = tag;
			return this;
		}
	}

	@Override
	public String toString() {
		return "SearchCriteria{" + "excludeContent=" + excludeContent + ", createdBy='"
				+ createdBy + '\'' + ", lastModifiedBy='" + lastModifiedBy + '\''
				+ ", tag=" + tag + ", categoryOrders=" + categoryOrders + ", keyword='"
				+ keyword + '\'' + '}';
	}
}
