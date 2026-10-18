package com.zeromods.core.filter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/** Reverses the previous legacy-list update while retaining later manual edits. */
public final class LegacyListUpdates {
  private LegacyListUpdates() {}

  public record Selection(int categories, List<FilterTarget> included, List<FilterTarget> excluded) {
    public Selection {
      included = List.copyOf(included);
      excluded = List.copyOf(excluded);
    }

    public static Selection capture(int categories, TargetExceptions exceptions) {
      return new Selection(categories, List.copyOf(exceptions.included()), List.copyOf(exceptions.excluded()));
    }

    public void applyTo(TargetExceptions exceptions) {
      exceptions.clear();
      exceptions.included().addAll(included);
      exceptions.excluded().addAll(excluded);
    }
  }

  public static Selection restore(Selection before, Selection applied, Selection current) {
    var result = new TargetExceptions();
    current.applyTo(result);
    var targets = new LinkedHashSet<FilterTarget>();
    targets.addAll(before.included());
    targets.addAll(before.excluded());
    targets.addAll(applied.included());
    targets.addAll(applied.excluded());
    for (var target : targets) {
      var original = entry(before, target);
      var written = entry(applied, target);
      // A subsequent manual removal, move or rename takes precedence over our old write.
      if (Objects.equals(original, written) || !Objects.equals(entry(current, target), written)) continue;
      result.included().removeIf(target::sameEntry);
      result.excluded().removeIf(target::sameEntry);
      if (original != null) result.add(original.target(), original.excluded());
    }
    int changed = before.categories() ^ applied.categories();
    int categories = (current.categories() & ~changed) | (before.categories() & changed);
    return Selection.capture(categories, result);
  }

  private record Entry(FilterTarget target, boolean excluded) {}

  private static Entry entry(Selection selection, FilterTarget target) {
    for (var candidate : selection.excluded()) if (target.sameEntry(candidate)) return new Entry(candidate, true);
    for (var candidate : selection.included()) if (target.sameEntry(candidate)) return new Entry(candidate, false);
    return null;
  }
}
