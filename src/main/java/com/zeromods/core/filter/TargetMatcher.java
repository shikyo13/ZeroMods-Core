package com.zeromods.core.filter;

import java.util.UUID;

/** Decides whether an exception names a subject. Wrap {@link #STANDARD} to add mod kinds. */
@FunctionalInterface
public interface TargetMatcher {
  boolean matches(FilterTarget target, EntitySelection.Subject subject, UUID owner);

  /** Matches the kinds defined by {@link FilterTarget}; unknown kinds never match. */
  TargetMatcher STANDARD = (target, subject, owner) -> switch (target.kind()) {
    case FilterTarget.MOB -> subject.entityType(target.id());
    case FilterTarget.ITEM -> subject.itemType(target.id());
    case FilterTarget.INDIVIDUAL -> subject.identity().toString().equalsIgnoreCase(target.id());
    case FilterTarget.PLAYER -> subject.category() == EntityCategories.PLAYER
        && subject.identity().toString().equalsIgnoreCase(target.id());
    default -> false;
  };
}
