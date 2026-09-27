package com.zeromods.core.filter;

import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * Explicit exceptions to a category selection. A subject named by an excluded target never
 * matches; otherwise one named by an included target always matches; everything else follows the
 * categories. Spectators and an exempt owner are never matched by either list.
 *
 * <p>The sets are live and ordered as entered. A target sits in at most one of them when added
 * through {@link #add}.
 */
public final class TargetExceptions {
  private final LinkedHashSet<FilterTarget> included = new LinkedHashSet<>(),
      excluded = new LinkedHashSet<>();

  public LinkedHashSet<FilterTarget> included() { return included; }

  public LinkedHashSet<FilterTarget> excluded() { return excluded; }

  public LinkedHashSet<FilterTarget> list(boolean exclude) { return exclude ? excluded : included; }

  /** Adds a target, moving it if the other list already names the same entry. */
  public void add(FilterTarget target, boolean exclude) {
    included.removeIf(target::sameEntry);
    excluded.removeIf(target::sameEntry);
    list(exclude).add(target);
  }

  public void clear() {
    included.clear();
    excluded.clear();
  }

  public void copyFrom(TargetExceptions source) {
    clear();
    included.addAll(source.included);
    excluded.addAll(source.excluded);
  }

  /**
   * Evaluates the exceptions, then the selection for everything they do not name. The selection
   * should not be inverted; {@link LegacyLists} folds inversion into categories and exceptions.
   */
  public boolean matches(EntitySelection selection, EntitySelection.Subject subject, UUID owner,
      TargetMatcher matcher) {
    if (!eligible(selection, subject, owner)) return false;
    if (names(excluded, subject, owner, matcher)) return false;
    if (names(included, subject, owner, matcher)) return true;
    return selection.matches(subject, owner);
  }

  /** Whether an included target names an eligible subject. */
  public boolean includes(EntitySelection selection, EntitySelection.Subject subject, UUID owner,
      TargetMatcher matcher) {
    return eligible(selection, subject, owner) && names(included, subject, owner, matcher);
  }

  private static boolean eligible(EntitySelection selection, EntitySelection.Subject subject,
      UUID owner) {
    return subject != null && !subject.spectator()
        && !(selection.exemptOwner() && subject.identity().equals(owner));
  }

  private static boolean names(LinkedHashSet<FilterTarget> targets,
      EntitySelection.Subject subject, UUID owner, TargetMatcher matcher) {
    for (var target : targets) if (matcher.matches(target, subject, owner)) return true;
    return false;
  }
}
