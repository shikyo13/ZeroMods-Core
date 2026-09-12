package com.zeromods.core.network;
import java.util.*;
/** The shared authoritative network model. N is a stable node address in the host directory's scope.
 * Minecraft adapters own codecs and dirty notifications, not parallel identity/permission models.
 * All mutation is server-thread confined. Snapshots never expose mutable collections.
 */
public final class ManagedNetwork<N> {
    public record Snapshot<N>(UUID id, String kind, String name, UUID owner, Set<UUID> members,
            Set<N> nodes, N anchor, boolean publicUse, boolean discoverable, Map<String,String> properties) {
        public Snapshot { members = Set.copyOf(members); nodes = Set.copyOf(nodes); properties = Map.copyOf(properties); }
    }
    private final UUID id;
    private final String kind;
    private final UUID owner;
    private String name;
    private N anchor;
    private boolean publicUse, discoverable;
    private final Set<UUID> members = new LinkedHashSet<>();
    private final Set<N> nodes = new LinkedHashSet<>();
    private final Map<String,String> properties = new LinkedHashMap<>();
    public ManagedNetwork(UUID id, String kind, String name, UUID owner) {
        this.id = Objects.requireNonNull(id); this.kind = Objects.requireNonNull(kind); this.owner = owner;
        if (!kind.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("Namespaced network kind required");
        rename(name);
    }
    public UUID id() { return id; }
    public String kind() { return kind; }
    public String name() { return name; }
    public UUID owner() { return owner; }
    public Optional<N> anchor() { return Optional.ofNullable(anchor); }
    public Set<UUID> memberIds() { return Collections.unmodifiableSet(members); }
    public Set<N> nodes() { return Collections.unmodifiableSet(nodes); }
    public void rename(String value) { Objects.requireNonNull(value); if (value.length() > 128) throw new IllegalArgumentException("Name too long"); name = value; }
    public void addMember(UUID member) { members.add(Objects.requireNonNull(member)); }
    public void removeMember(UUID member) { members.remove(member); }
    public void addNode(N node) { nodes.add(Objects.requireNonNull(node)); if (anchor == null) anchor = node; }
    public void removeNode(N node) { nodes.remove(node); }
    /** Anchor is historical: unloading or removing a node does not move the displayed origin. */
    public void anchor(N node) { anchor = Objects.requireNonNull(node); }
    public void access(boolean publicUse, boolean discoverable) { this.publicUse = publicUse; this.discoverable = discoverable; }
    public boolean isOwner(UUID actor) { return actor != null && actor.equals(owner); }
    public boolean canConfigure(UUID actor) { return actor != null && (isOwner(actor) || members.contains(actor)); }
    public boolean canUse(UUID actor) { return actor != null && (canConfigure(actor) || publicUse); }
    public boolean canDiscover(UUID actor) { return actor != null && (canConfigure(actor) || publicUse || discoverable); }
    public NetworkAccess access() { return new NetworkAccess(owner, members, publicUse, discoverable); }
    public void property(String key, String value) {
        if (key == null || !key.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || value == null || value.length() > 4096)
            throw new IllegalArgumentException("Invalid network property");
        properties.put(key, value);
    }
    public Optional<String> property(String key) { return Optional.ofNullable(properties.get(key)); }
    public Snapshot<N> snapshot() { return new Snapshot<>(id, kind, name, owner, members, nodes, anchor, publicUse, discoverable, properties); }
    public static <N> ManagedNetwork<N> restore(Snapshot<N> saved) {
        var network = new ManagedNetwork<N>(saved.id(), saved.kind(), saved.name(), saved.owner());
        saved.members().forEach(network::addMember); saved.nodes().forEach(network::addNode);
        network.anchor = saved.anchor(); network.access(saved.publicUse(), saved.discoverable());
        saved.properties().forEach(network::property); return network;
    }
}
