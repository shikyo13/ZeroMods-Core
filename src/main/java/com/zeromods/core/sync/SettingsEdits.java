package com.zeromods.core.sync;

import java.util.*;
import java.util.function.UnaryOperator;

/** Compare-and-set edits. Copies the snapshot before writing; callers retain their original.
 * Independent properties merge, repeated keys and invalid values fail, and an atomic
 * conflict discards the whole batch. The host supplies its schema and transport limits.
 */
public final class SettingsEdits {
  private SettingsEdits() {}

  public record Change<V>(String key, V before, V after) {}
  public record Result<S>(S settings, boolean conflict) {}

  public interface Property<S, V> {
    V read(S settings);
    boolean accepts(V value);
    void write(S settings, V value);
  }

  public static <S, V> Result<S> apply(S current, List<Change<V>> changes,
      Map<String, ? extends Property<S, V>> schema, UnaryOperator<S> copy, boolean atomic) {
    if (changes.size() > schema.size()) throw new IllegalArgumentException("Too many settings edits");
    S result = copy.apply(current);
    var seen = new HashSet<String>();
    boolean conflict = false;
    for (var change : changes) {
      var property = schema.get(change.key());
      if (property == null || !seen.add(change.key()))
        throw new IllegalArgumentException("Unknown or repeated property");
      if (!property.accepts(change.before()) || !property.accepts(change.after()))
        throw new IllegalArgumentException("Invalid property value");
      var value = property.read(current);
      if (Objects.equals(value, change.after())) continue;
      if (!Objects.equals(value, change.before())) { conflict = true; continue; }
      property.write(result, change.after());
    }
    return new Result<>(atomic && conflict ? copy.apply(current) : result, conflict);
  }
}
