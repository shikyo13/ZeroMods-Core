package com.zeromods.core.filter;
import java.util.*;
import java.util.function.Predicate;
/** Per-mod registry; namespaced IDs allow new entity categories without changing a shared enum. */
public final class FilterRegistry<T> {
    public record Category<T>(String id, String labelKey, String tooltipKey, Predicate<T> matches) {
        public Category {
            Objects.requireNonNull(id); Objects.requireNonNull(labelKey); Objects.requireNonNull(tooltipKey); Objects.requireNonNull(matches);
            if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("Use a namespaced category ID");
        }
    }
    private final Map<String,Category<T>> categories = new LinkedHashMap<>();
    public void register(Category<T> category) {
        if (categories.putIfAbsent(category.id(), category) != null) throw new IllegalArgumentException("Duplicate category: " + category.id());
    }
    public Category<T> require(String id) {
        var category = categories.get(id); if (category == null) throw new IllegalArgumentException("Unknown category: " + id); return category;
    }
    public List<Category<T>> categories() { return List.copyOf(categories.values()); }
}
