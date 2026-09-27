package com.zeromods.core.filter;

/** Category bits reported by {@link EntitySelection.Subject#category()}. Each entity has one. */
public final class EntityCategories {
  public static final int HOSTILE = 1, PASSIVE = 2, PLAYER = 4, ITEM = 8, NONLIVING = 16,
      PROJECTILE = 32;
  public static final int ALL = 63;

  /** Categories saved before projectiles had their own bit, when they counted as nonliving. */
  public static final int LEGACY_ALL = 31;

  private EntityCategories() {}

  /** Reads a legacy selection so that projectiles stay selected wherever nonliving was. */
  public static int fromLegacy(int categories) {
    categories &= LEGACY_ALL;
    return (categories & NONLIVING) != 0 ? categories | PROJECTILE : categories;
  }
}
