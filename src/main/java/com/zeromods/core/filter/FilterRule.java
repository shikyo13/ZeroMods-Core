package com.zeromods.core.filter;
import java.util.*;
import java.util.function.Predicate;
/** Immutable predicate composition: categories OR, constraints AND, inversion last.
 * Eligibility and exemptions are outside inversion (spectators/owners never become targets).
 */
public final class FilterRule<T> implements Predicate<T> {
    private final Predicate<T> eligible;
    private final List<Predicate<T>> categories, constraints;
    private final boolean inverted;
    public FilterRule(Predicate<T> eligible, List<Predicate<T>> categories,
            List<Predicate<T>> constraints, boolean inverted) {
        this.eligible = Objects.requireNonNull(eligible); this.categories = List.copyOf(categories);
        this.constraints = List.copyOf(constraints); this.inverted = inverted;
    }
    public boolean test(T target) {
        if (target == null || !eligible.test(target)) return false;
        boolean match = categories.stream().anyMatch(p -> p.test(target))
                && constraints.stream().allMatch(p -> p.test(target));
        return inverted != match;
    }
    /** Same truth table for adapters that already evaluate Minecraft tags and entity properties. */
    public static boolean result(boolean eligible, boolean categoryMatch, boolean constraintsMatch, boolean inverted) {
        return eligible && (inverted != (categoryMatch && constraintsMatch));
    }
}
