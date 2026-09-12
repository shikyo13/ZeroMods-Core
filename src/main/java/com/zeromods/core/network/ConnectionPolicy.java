package com.zeromods.core.network;
import java.util.*;
/** Geometry and virtual membership are policies over the same node/network model. */
@FunctionalInterface
public interface ConnectionPolicy<N> {
    boolean connects(N from, N to);
    default List<N> neighbors(N from, Collection<N> loadedNodes) {
        return loadedNodes.stream().filter(to -> !Objects.equals(from, to) && connects(from, to)).toList();
    }
    static <N> ConnectionPolicy<N> explicitMembers(Set<N> members) {
        Set<N> saved = Set.copyOf(members); return (from, to) -> saved.contains(from) && saved.contains(to);
    }
}
