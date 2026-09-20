package com.zeromods.core.network;

import java.util.*;

/** Reconciles persistent identities independently of reusable or relocated node addresses.
 * Invoke on the server thread before physical topology reconciliation. The host owns
 * identity persistence, dirty marking, legacy-node policy and its on-disk codec.
 */
public final class NodeIdentities {
  private NodeIdentities() {}

  public interface Node<N> {
    N position();
    UUID identity();
    void identity(UUID replacement);
    UUID owner();
    boolean replacement();
  }

  public interface Store<N> {
    Map<N, UUID> read(ManagedNetwork<N> network);
    void write(ManagedNetwork<N> network, Map<N, UUID> identities);
  }

  private record Member<N>(ManagedNetwork<N> network, N position) {}
  private record Move<N>(Member<N> previous, N destination, UUID identity) {}

  public static <N> void reconcile(NetworkDirectory<N> directory,
      List<? extends Node<N>> loaded, Store<N> store) {
    var identities = new HashMap<ManagedNetwork<N>, Map<N, UUID>>();
    var previous = new HashMap<UUID, Member<N>>();
    for (var snapshot : directory.snapshot()) {
      var network = directory.get(snapshot.id()).orElseThrow();
      var ids = new HashMap<>(store.read(network));
      ids.keySet().retainAll(network.nodes());
      identities.put(network, ids);
      ids.forEach((pos, id) -> previous.putIfAbsent(id, new Member<N>(network, pos)));
    }
    var moves = new ArrayList<Move<N>>();
    var claimed = new HashSet<UUID>();
    var ordered = new ArrayList<>(loaded);
    // A copied block must not steal the original member's network identity.
    ordered.sort(Comparator.comparing(e -> {
      var old = previous.get(e.identity());
      return old == null || !old.position.equals(e.position());
    }));
    for (var node : ordered) {
      if (!claimed.add(node.identity())) {
        node.identity(UUID.randomUUID());
        claimed.add(node.identity());
      }
      var pos = node.position();
      var old = previous.get(node.identity());
      for (var entry : identities.entrySet()) {
        var network = entry.getKey();
        var known = entry.getValue().get(pos);
        if (network.nodes().contains(pos)
            && (known != null && !known.equals(node.identity())
                || known == null && node.replacement())) {
          network.removeNode(pos);
          entry.getValue().remove(pos);
        }
      }
      if (old != null && !old.position.equals(pos)
          && Objects.equals(old.network.owner(), node.owner())) {
        moves.add(new Move<N>(old, pos, node.identity()));
      }
    }
    for (var move : moves) {
      move.previous.network.removeNode(move.previous.position);
      identities.get(move.previous.network).remove(move.previous.position);
      directory.setLoaded(move.previous.position, false);
    }
    for (var move : moves) {
      move.previous.network.addNode(move.destination);
      identities.get(move.previous.network).put(move.destination, move.identity);
    }
    for (var entry : identities.entrySet()) {
      if (entry.getKey().nodes().isEmpty()) directory.remove(entry.getKey().id());
      else store.write(entry.getKey(), entry.getValue());
    }
  }

}
