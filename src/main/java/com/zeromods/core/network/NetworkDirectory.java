package com.zeromods.core.network;
import java.util.*;
/** Per-world/server-thread repository for the one Core network model.
 * Unload and delete are distinct. Availability is transient and never serialized.
 */
public final class NetworkDirectory<N> {
    private final Map<UUID,ManagedNetwork<N>> networks = new LinkedHashMap<>();
    private final Set<N> loaded = new HashSet<>();
    public void put(ManagedNetwork<N> network) {
        Objects.requireNonNull(network);
        if (networks.putIfAbsent(network.id(), network) != null) throw new IllegalArgumentException("Duplicate network ID");
    }
    public Optional<ManagedNetwork<N>> get(UUID id) { return Optional.ofNullable(networks.get(id)); }
    public void setLoaded(N node, boolean available) { Objects.requireNonNull(node); if (available) loaded.add(node); else loaded.remove(node); }
    public List<N> loadedNodes(UUID id) { return get(id).map(e -> e.nodes().stream().filter(loaded::contains).toList()).orElse(List.of()); }
    public void remove(UUID id) { networks.remove(id); }
    public List<ManagedNetwork.Snapshot<N>> snapshot() { return networks.values().stream().map(ManagedNetwork::snapshot).toList(); }
    public void restore(Collection<ManagedNetwork.Snapshot<N>> saved) {
        var replacement = new LinkedHashMap<UUID,ManagedNetwork<N>>();
        for (var entry : saved) {
            var network = ManagedNetwork.restore(entry);
            if (replacement.putIfAbsent(network.id(), network) != null) throw new IllegalArgumentException("Duplicate network ID");
        }
        networks.clear(); networks.putAll(replacement); loaded.clear();
    }
    /** Explicit same-owner merge: retain chosen survivor identity/name/anchor. Never auto-promote permissions. */
    public void merge(UUID survivorId, UUID absorbedId) {
        if (survivorId.equals(absorbedId)) return;
        var survivor = get(survivorId).orElseThrow(); var absorbed = get(absorbedId).orElseThrow();
        if (!Objects.equals(survivor.owner(), absorbed.owner()) || !survivor.kind().equals(absorbed.kind()))
            throw new IllegalArgumentException("Incompatible network owners or kinds");
        absorbed.nodes().forEach(survivor::addNode); networks.remove(absorbedId);
    }
    /** Explicit split; caller supplies a persisted new ID. Unloaded nodes stay with the original network. */
    public ManagedNetwork<N> split(UUID originalId, UUID newId, String name, Set<N> moved) {
        var original = get(originalId).orElseThrow();
        if (networks.containsKey(newId) || moved.isEmpty() || !original.nodes().containsAll(moved) || moved.size() == original.nodes().size())
            throw new IllegalArgumentException("Invalid split");
        var created = new ManagedNetwork<N>(newId, original.kind(), name, original.owner());
        original.memberIds().forEach(created::addMember);
        var saved = original.snapshot(); created.access(saved.publicUse(), saved.discoverable());
        saved.properties().forEach(created::property);
        moved.forEach(created::addNode); moved.forEach(original::removeNode); put(created); return created;
    }
    public void clear() { networks.clear(); loaded.clear(); }
}
