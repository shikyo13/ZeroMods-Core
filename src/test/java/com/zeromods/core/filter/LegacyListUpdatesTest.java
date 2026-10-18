package com.zeromods.core.filter;

import java.util.List;

/** Dependency-free regression checks, run by the check task. */
public final class LegacyListUpdatesTest {
  private static final FilterTarget CREEPER = new FilterTarget(FilterTarget.MOB, "minecraft:creeper", "");
  private static final FilterTarget ZOMBIE = new FilterTarget(FilterTarget.MOB, "minecraft:zombie", "");
  private static final FilterTarget ITEM = new FilterTarget(FilterTarget.ITEM, "minecraft:diamond", "");

  public static void main(String[] args) {
    var exceptions = new TargetExceptions();
    exceptions.add(ITEM, true);
    var before = LegacyListUpdates.Selection.capture(EntityCategories.HOSTILE, exceptions);
    int groups = lists(before.categories(), "minecraft:creeper").foldInto(exceptions);
    var applied = LegacyListUpdates.Selection.capture(groups, exceptions);
    var restored = LegacyListUpdates.restore(before, applied, applied);
    require(restored.equals(before), "reset restores category bits and unrelated exceptions");
    restored.applyTo(exceptions);
    groups = lists(restored.categories(), "minecraft:zombie").foldInto(exceptions);
    require(!exceptions.included().contains(CREEPER) && exceptions.included().contains(ZOMBIE),
        "replacement removes the old list");
    require(exceptions.excluded().contains(ITEM), "replacement preserves unrelated exclusions");

    applied.applyTo(exceptions);
    exceptions.add(ZOMBIE, false);
    var manual = LegacyListUpdates.Selection.capture(groups | EntityCategories.PROJECTILE, exceptions);
    restored = LegacyListUpdates.restore(before, applied, manual);
    require(restored.included().contains(ZOMBIE), "reset preserves later manual additions");
    require((restored.categories() & EntityCategories.PROJECTILE) != 0, "reset preserves unrelated categories");

    exceptions.clear();
    exceptions.add(CREEPER, true);
    before = LegacyListUpdates.Selection.capture(0, exceptions);
    groups = lists(0, "minecraft:creeper").foldInto(exceptions);
    applied = LegacyListUpdates.Selection.capture(groups, exceptions);
    require(LegacyListUpdates.restore(before, applied, applied).equals(before),
        "reset restores an exception displaced by the script");
    exceptions.clear();
    manual = LegacyListUpdates.Selection.capture(groups, exceptions);
    require(LegacyListUpdates.restore(before, applied, manual).excluded().isEmpty(),
        "manual removal is not undone");
    System.out.println("Legacy list update regressions passed");
  }

  private static LegacyLists lists(int groups, String id) {
    return new LegacyLists(groups, false, 1, List.of(id), 0, List.of(), 0, List.of());
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
