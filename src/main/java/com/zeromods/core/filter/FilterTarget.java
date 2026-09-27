package com.zeromods.core.filter;

import java.util.Objects;

/**
 * A named exception to a filter's category selection. Core defines the standard kinds; a mod may
 * use its own kind strings and match them with a {@link TargetMatcher}.
 */
public record FilterTarget(String kind, String id, String name) {
  /** An entity type ID or {@code #tag}. */
  public static final String MOB = "MOB";
  /** A dropped item's item ID or {@code #tag}. */
  public static final String ITEM = "ITEM";
  /** A player UUID; {@code name} is the last known player name. */
  public static final String PLAYER = "PLAYER";
  /** One entity's UUID; {@code name} is its entity type ID. */
  public static final String INDIVIDUAL = "INDIVIDUAL";

  public FilterTarget {
    Objects.requireNonNull(kind);
    Objects.requireNonNull(id);
    Objects.requireNonNull(name);
  }

  /** Whether both targets name the same thing, regardless of the display name. */
  public boolean sameEntry(FilterTarget other) {
    return kind.equals(other.kind) && id.equals(other.id);
  }
}
