package com.zeromods.core.settings;
import java.util.*;
import java.util.function.Predicate;
/** Typed settings metadata shared by UI and server validation; labels are translation keys. */
public record Setting<T>(String id, Class<T> type, T defaultValue, String labelKey, String tooltipKey, Predicate<T> valid) {
    public Setting {
        Objects.requireNonNull(id); Objects.requireNonNull(type); Objects.requireNonNull(defaultValue);
        Objects.requireNonNull(labelKey); Objects.requireNonNull(tooltipKey); Objects.requireNonNull(valid);
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || !type.isInstance(defaultValue) || !valid.test(defaultValue))
            throw new IllegalArgumentException("Invalid setting definition: " + id);
    }
    public T validate(Object value) {
        if (!type.isInstance(value) || !valid.test(type.cast(value))) throw new IllegalArgumentException("Invalid value for " + id);
        return type.cast(value);
    }
}
