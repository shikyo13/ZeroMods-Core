package com.zeromods.core.filter;

import java.util.Collection;
import java.util.List;

/**
 * List settings from before exceptions existed. Each mode is 0 to follow the categories, 1 to
 * match only listed entries of that kind, or 2 to match all but the listed entries. Folding
 * rewrites them, and an inverted selection, as categories plus exceptions with the same results.
 *
 * @param categories selection in current bits; convert saved values with
 *     {@link EntityCategories#fromLegacy} first
 * @param players player targets for the player list, such as {@link FilterTarget#PLAYER} entries
 *     and any mod-specific group kinds
 */
public record LegacyLists(int categories, boolean inverted, int mobMode, Collection<String> mobs,
    int itemMode, Collection<String> items, int playerMode, Collection<FilterTarget> players) {
  private static final int MOBS = EntityCategories.HOSTILE | EntityCategories.PASSIVE;

  public LegacyLists {
    mobs = List.copyOf(mobs);
    items = List.copyOf(items);
    players = List.copyOf(players);
  }

  /** Whether the folded entries fit in exception lists of the given size. */
  public boolean fits(int maxPerList) {
    for (int mode = 1; mode <= 2; mode++) {
      int count = (mobMode == mode ? mobs.size() : 0) + (itemMode == mode ? items.size() : 0)
          + (playerMode == mode ? players.size() : 0);
      if (count > maxPerList) return false;
    }
    return true;
  }

  /** Adds the listed entries to {@code exceptions} and returns the equivalent categories. */
  public int foldInto(TargetExceptions exceptions) {
    int result = inverted ? ~categories & EntityCategories.ALL : categories;
    if (mobMode != 0) {
      result = mobMode == 1 ? result & ~MOBS : result | MOBS;
      for (String id : mobs) exceptions.add(new FilterTarget(FilterTarget.MOB, id, ""), mobMode == 2);
    }
    if (itemMode != 0) {
      result = itemMode == 1 ? result & ~EntityCategories.ITEM : result | EntityCategories.ITEM;
      for (String id : items) exceptions.add(new FilterTarget(FilterTarget.ITEM, id, ""), itemMode == 2);
    }
    if (playerMode != 0) {
      result = playerMode == 1 ? result & ~EntityCategories.PLAYER : result | EntityCategories.PLAYER;
      for (var target : players) exceptions.add(target, playerMode == 2);
    }
    return result;
  }
}
