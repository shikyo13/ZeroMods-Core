package com.zeromods.core.network;
import java.util.*;
/** Adapts physical components into the same model used by explicitly assigned networks.
 * Split is deferred while any previous node is unobserved (typically an unloaded chunk).
 * Call on the server thread with deterministic component/node order, and observed positions
 * including removed nodes. This method never loads chunks or invents deletion on unload.
 */
public final class PhysicalNetworkReconciler<N> {
    public record Component<N>(UUID owner, String initialName, List<N> nodes) {
        public Component { nodes = List.copyOf(nodes); if (nodes.isEmpty()) throw new IllegalArgumentException("Empty component"); }
    }
    private final NetworkDirectory<N> directory;
    private final String kind;
    public PhysicalNetworkReconciler(NetworkDirectory<N> directory, String kind) {
        this.directory = Objects.requireNonNull(directory); this.kind = Objects.requireNonNull(kind);
    }
    public Map<N,UUID> reconcile(List<Component<N>> components, Set<N> observed) {
        var present = new LinkedHashSet<N>();
        for (var component : components) for (var node : component.nodes())
            if (!present.add(node)) throw new IllegalArgumentException("Node in multiple physical components");
        if (!observed.containsAll(present)) throw new IllegalArgumentException("Components must be observed");
        var owners = new HashMap<N,UUID>();
        components.forEach(component -> component.nodes().forEach(node -> owners.put(node,component.owner())));
        for (var snapshot : directory.snapshot()) if (snapshot.kind().equals(kind)) {
            var network = directory.get(snapshot.id()).orElseThrow();
            for (N node : snapshot.nodes()) {
                directory.setLoaded(node, present.contains(node));
                if (observed.contains(node) && (!present.contains(node) || !Objects.equals(owners.get(node), network.owner()))) network.removeNode(node);
            }
            if (network.nodes().isEmpty()) directory.remove(network.id());
        }
        var assigned = new HashSet<UUID>(); var result = new LinkedHashMap<N,UUID>();
        for (var component : components) {
            var candidates = directory.snapshot().stream()
                .filter(n -> n.kind().equals(kind) && Objects.equals(n.owner(), component.owner()))
                .filter(n -> n.nodes().stream().anyMatch(component.nodes()::contains))
                .sorted(Comparator.<ManagedNetwork.Snapshot<N>,Boolean>comparing(n -> !Objects.equals(component.nodes().get(0),n.anchor()))
                    .thenComparing(n -> !component.nodes().contains(n.anchor()))
                    .thenComparing(n -> n.id().toString())).toList();
            ManagedNetwork<N> target = null;
            for (var candidate : candidates) {
                if (!assigned.contains(candidate.id()) || !observed.containsAll(candidate.nodes())) {
                    target = directory.get(candidate.id()).orElseThrow(); break;
                }
            }
            if (target == null) {
                target = new ManagedNetwork<>(UUID.randomUUID(), kind,
                    candidates.isEmpty() ? component.initialName() : candidates.get(0).name(), component.owner());
                directory.put(target);
                if (!candidates.isEmpty()) {
                    var prior=candidates.get(0); prior.members().forEach(target::addMember);
                    target.access(prior.publicUse(),prior.discoverable()); prior.properties().forEach(target::property);
                }
            }
            for (var candidate : candidates) {
                if (candidate.id().equals(target.id())) continue;
                if (!assigned.contains(candidate.id())) directory.merge(target.id(), candidate.id());
                else {
                    var previous=directory.get(candidate.id()).orElse(null);
                    if(previous != null) component.nodes().forEach(previous::removeNode);
                }
            }
            for (N node : component.nodes()) { target.addNode(node); directory.setLoaded(node,true); result.put(node,target.id()); }
            assigned.add(target.id());
        }
        return Map.copyOf(result);
    }
}
