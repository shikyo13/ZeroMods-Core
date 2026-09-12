package com.zeromods.core.network;
import java.util.*;
import java.util.function.Function;
/** Deterministic breadth-first traversal. Adapters supply loaded neighbors, without loading chunks. */
public final class NetworkTraversal {
    private NetworkTraversal() {}
    public static <N,K> List<N> connected(N seed, Function<N,K> key,
            Function<N,? extends Iterable<N>> neighbors) {
        Objects.requireNonNull(seed); Objects.requireNonNull(key); Objects.requireNonNull(neighbors);
        var result = new ArrayList<N>(); var seen = new HashSet<K>(); var queue = new ArrayDeque<N>();
        seen.add(Objects.requireNonNull(key.apply(seed))); queue.add(seed);
        while (!queue.isEmpty()) {
            N node = queue.remove(); result.add(node);
            for (N next : neighbors.apply(node)) {
                if (seen.add(Objects.requireNonNull(key.apply(next)))) queue.add(next);
            }
        }
        return result;
    }
}
