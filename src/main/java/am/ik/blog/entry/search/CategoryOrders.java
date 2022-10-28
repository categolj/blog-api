package am.ik.blog.entry.search;

import am.ik.blog.category.Category;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class CategoryOrders {

    private final Set<CategoryOrder> value = new LinkedHashSet<>();

    public CategoryOrders() {
    }

    public static CategoryOrders from(List<String> list) {
        List<Category> categories = list.stream()
                .map(Category::new).toList();
        int order = categories.size() - 1;
        Category category = categories.get(order);
        return new CategoryOrders().add(category, order) /* TODO */;
    }

    public CategoryOrders add(CategoryOrder categoryOrder) {
        value.add(categoryOrder);
        return this;
    }

    public CategoryOrders add(Category category, int categoryOrder) {
        return add(new CategoryOrder(category, categoryOrder));
    }

    public Set<CategoryOrder> getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CategoryOrders.class.getSimpleName() + "[", "]")
            .add("value=" + value)
            .toString();
    }
}
