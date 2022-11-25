package am.ik.blog.entry.search;

import am.ik.blog.category.Category;

import java.util.StringJoiner;

public class CategoryOrder {

	private final Category category;

	private final int categoryOrder;

	public CategoryOrder(Category category, int categoryOrder) {
		this.category = category;
		this.categoryOrder = categoryOrder;
	}

	public Category getCategory() {
		return this.category;
	}

	public int getCategoryOrder() {
		return this.categoryOrder;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", CategoryOrder.class.getSimpleName() + "[", "]")
				.add("category=" + category).add("categoryOrder=" + categoryOrder)
				.toString();
	}
}
