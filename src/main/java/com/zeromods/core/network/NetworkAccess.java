package com.zeromods.core.network;
import java.util.*;
/** Public use never grants configuration rights. Discovery is separate from access. */
public record NetworkAccess(UUID owner, Set<UUID> members, boolean publicUse, boolean discoverable) {
    public NetworkAccess { members = Set.copyOf(members); }
    public boolean isOwner(UUID actor) { return actor != null && actor.equals(owner); }
    public boolean canConfigure(UUID actor) { return actor != null && (isOwner(actor) || members.contains(actor)); }
    public boolean canUse(UUID actor) { return actor != null && (canConfigure(actor) || publicUse); }
    public boolean canDiscover(UUID actor) { return actor != null && (canConfigure(actor) || publicUse || discoverable); }
}
