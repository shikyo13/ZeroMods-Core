package com.zeromods.core.filter;
import java.util.UUID;
/** Stable legacy category bits; custom mod categories use FilterRegistry/FilterRule.
 * The adapter resolves actual registry IDs/tags; the shared model owns filter semantics.
 */
public record EntitySelection(int categories, Age age, boolean inverted, boolean exemptOwner,
        String identity, String scoreboardTag, String entityType, String itemType) {
    public enum Age { ANY, BABY, ADULT }
    public interface Subject {
        UUID identity(); boolean spectator(); int category(); boolean living(); boolean baby();
        boolean scoreboardTag(String tag); boolean entityType(String idOrTag); boolean itemType(String idOrTag);
    }
    public EntitySelection {
        java.util.Objects.requireNonNull(age);java.util.Objects.requireNonNull(identity);
        java.util.Objects.requireNonNull(scoreboardTag);java.util.Objects.requireNonNull(entityType);java.util.Objects.requireNonNull(itemType);
    }
    public boolean matches(Subject target, UUID owner) {
        if(target==null || target.spectator() || exemptOwner && target.identity().equals(owner))return false;
        boolean constraints=age==Age.ANY || target.living() && target.baby()==(age==Age.BABY);
        if(!identity.isEmpty()) constraints &= target.identity().toString().equalsIgnoreCase(identity);
        if(!scoreboardTag.isEmpty()) constraints &= target.scoreboardTag(scoreboardTag);
        if(!entityType.isEmpty()) constraints &= target.entityType(entityType);
        if(!itemType.isEmpty()) constraints &= target.itemType(itemType);
        return FilterRule.result(true,(categories & target.category())!=0,constraints,inverted);
    }
}
