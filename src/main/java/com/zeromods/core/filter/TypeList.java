package com.zeromods.core.filter;

import java.util.Collection;
import java.util.function.Predicate;

/** ORs registry IDs/tags, applies selection details, then chooses listed or unlisted targets. */
public final class TypeList {
    private TypeList() {}
    public static boolean matches(int mode, Collection<String> entries, Predicate<String> matchesType, boolean details) {
        boolean selected = details && entries.stream().anyMatch(matchesType);
        return switch (mode) {
            case 1 -> selected;
            case 2 -> !selected;
            default -> throw new IllegalArgumentException("A type list must be enabled before matching");
        };
    }
}
