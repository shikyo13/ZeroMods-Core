package com.zeromods.core.filter;
import java.util.*;
/** Optional direction-specific rules with a shared fallback. Directions are host-defined stable keys. */
public final class DirectionalRules<D,R> {
    private final Map<D,R> overrides = new LinkedHashMap<>();
    public R resolve(D direction, R shared) { return overrides.getOrDefault(direction,shared); }
    public boolean has(D direction) { return overrides.containsKey(direction); }
    public void set(D direction,R rule) { overrides.put(Objects.requireNonNull(direction),Objects.requireNonNull(rule)); }
    public void inherit(D direction) { overrides.remove(direction); }
    public Map<D,R> overrides() { return Collections.unmodifiableMap(overrides); }
}
