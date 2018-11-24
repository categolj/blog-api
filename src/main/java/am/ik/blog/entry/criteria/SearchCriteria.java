package am.ik.blog.entry.criteria;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import am.ik.blog.entry.Name;
import am.ik.blog.entry.Tag;

import org.springframework.util.StringUtils;

public class SearchCriteria {
	public static final SearchCriteria DEFAULT = defaults().build();

	private boolean excludeContent;
	private Name createdBy;
	private Name lastModifiedBy;
	private Tag tag;
	private CategoryOrders categoryOrders;
	private String keyword;

	SearchCriteria(boolean excludeContent, Name createdBy, Name lastModifiedBy, Tag tag,
			CategoryOrders categoryOrders, String keyword) {
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
		return SearchCriteria.builder().excludeContent(true);
	}

	public CategoryOrders getCategoryOrders() {
		return this.categoryOrders;
	}

	public Name getCreatedBy() {
		return this.createdBy;
	}

	public String getKeyword() {
		return this.keyword;
	}

	public Name getLastModifiedBy() {
		return this.lastModifiedBy;
	}

	public Tag getTag() {
		return this.tag;
	}

	public boolean isExcludeContent() {
		return this.excludeContent;
	}

	public String toJoinClause() {
		StringBuilder sb = new StringBuilder();
		if (this.tag != null) {
			sb.append("LEFT JOIN entry_tag AS et ON e.entry_id = et.entry_id ");
		}
		if (this.categoryOrders != null) {
			sb.append("LEFT JOIN category AS c ON e.entry_id = c.entry_id ");
		}
		return sb.toString();
	}

	public ClauseAndParams toWhereClause() {
		AtomicInteger i = new AtomicInteger(1);
		Map<String, String> clause = new LinkedHashMap<>();
		Map<String, Object> params = new HashMap<>();
		if (!StringUtils.isEmpty(this.keyword)) {
			params.put("$" + i, "%" + this.keyword + "%");
			clause.put("$" + i, "AND e.content LIKE $" + i);
			i.incrementAndGet();
		}
		if (this.createdBy != null) {
			params.put("$" + i, this.createdBy.getValue());
			clause.put("$" + i, "AND e.created_by = $" + i);
			i.incrementAndGet();
		}
		if (this.lastModifiedBy != null) {
			params.put("$" + i, this.lastModifiedBy.getValue());
			clause.put("$" + i, "AND e.last_modified_by = $" + i);
			i.incrementAndGet();
		}
		if (this.tag != null) {
			params.put("$" + i, this.tag.getValue());
			clause.put("$" + i, "AND et.tag_name = $" + i);
			i.incrementAndGet();
		}
		if (this.categoryOrders != null) {
			this.categoryOrders.getValue().forEach(c -> {
				int categoryOrder = c.getCategoryOrder();
				String categoryNameKey = "$" + i;
				String categoryOrderKey = "$" + i.incrementAndGet();
				params.put(categoryNameKey, c.getCategory().getValue());
				clause.put(categoryNameKey, "AND c.category_name = " + categoryNameKey);
				params.put(categoryOrderKey, categoryOrder);
				clause.put(categoryOrderKey,
						"AND c.category_order = " + categoryOrderKey);
				i.incrementAndGet();
			});
		}
		return new ClauseAndParams(clause, params);
	}

	public static class ClauseAndParams {
		private final Map<String, String> clause;
		private final Map<String, Object> params;

		ClauseAndParams(Map<String, String> clause, Map<String, Object> params) {
			this.clause = clause;
			this.params = params;
		}

		public String clauseForEntryId() {
			return String.join(" ", this.clause.values());
		}

		public Map<String, Object> params() {
			return this.params;
		}
	}

	public static class SearchCriteriaBuilder {

		private CategoryOrders categoryOrders;

		private Name createdBy;

		private boolean excludeContent;

		private String keyword;

		private Name lastModifiedBy;

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

		public SearchCriteriaBuilder createdBy(Name createdBy) {
			this.createdBy = createdBy;
			return this;
		}

		public SearchCriteriaBuilder excludeContent(boolean excludeContent) {
			this.excludeContent = excludeContent;
			return this;
		}

		public SearchCriteriaBuilder keyword(String keyword) {
			this.keyword = keyword;
			return this;
		}

		public SearchCriteriaBuilder lastModifiedBy(Name lastModifiedBy) {
			this.lastModifiedBy = lastModifiedBy;
			return this;
		}

		public SearchCriteriaBuilder tag(Tag tag) {
			this.tag = tag;
			return this;
		}

		public String toString() {
			return "SearchCriteria.SearchCriteriaBuilder(excludeContent="
					+ this.excludeContent + ", createdBy=" + this.createdBy
					+ ", lastModifiedBy=" + this.lastModifiedBy + ", tag=" + this.tag
					+ ", categoryOrders=" + this.categoryOrders + ", keyword="
					+ this.keyword + ")";
		}
	}
}
