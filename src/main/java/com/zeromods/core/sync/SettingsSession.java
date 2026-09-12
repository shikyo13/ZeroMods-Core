package com.zeromods.core.sync;
import com.zeromods.core.settings.*;
import java.util.*;
import java.util.function.Predicate;
/** Server-thread, per-target settings. Call only after resolving a loaded target from a packet.
 * The host supplies current permissions. Revision prevents an old GUI overwriting newer edits.
 * Transport and disk codecs remain version-specific adapters.
 */
public final class SettingsSession<A> {
    public enum Result { APPLIED, UNCHANGED, DENIED, STALE, INVALID }
    public record Snapshot(long revision, Map<String,Object> values) {
        public Snapshot { values = Map.copyOf(values); }
    }
    private final SettingsSchema schema;
    private final Predicate<A> canConfigure;
    private final Map<String,Object> values = new LinkedHashMap<>();
    private long revision;
    public SettingsSession(SettingsSchema schema, Predicate<A> canConfigure) {
        this.schema = Objects.requireNonNull(schema); this.canConfigure = Objects.requireNonNull(canConfigure);
    }
    public Result apply(A actor, long expectedRevision, String key, Object value) {
        if (actor == null || !canConfigure.test(actor)) return Result.DENIED;
        if (expectedRevision != revision) return Result.STALE;
        Setting<?> setting;
        try { setting = schema.require(key); value = setting.validate(value); }
        catch (IllegalArgumentException exception) { return Result.INVALID; }
        if (Objects.equals(values.getOrDefault(key, setting.defaultValue()), value)) return Result.UNCHANGED;
        values.put(key, value); revision++; return Result.APPLIED;
    }
    public Snapshot snapshot() {
        var complete = new LinkedHashMap<String,Object>();
        for (var setting : schema.settings()) complete.put(setting.id(), values.getOrDefault(setting.id(), setting.defaultValue()));
        return new Snapshot(revision, complete);
    }
}
