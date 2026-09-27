package com.zeromods.core.filter;

import java.util.Collection;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

/** NBT form of {@link FilterTarget}: {@code {Kind, Id, Name}}. */
public final class FilterTargetTags {
  private static final int MAX_KIND_LENGTH = 32;

  private FilterTargetTags() {}

  public static CompoundTag save(FilterTarget target) {
    var tag = new CompoundTag();
    tag.putString("Kind", target.kind());
    tag.putString("Id", target.id());
    tag.putString("Name", target.name());
    return tag;
  }

  /** Returns {@code null} for an entry with a missing kind or an over-long ID or name. */
  public static FilterTarget load(CompoundTag tag, int maxLength) {
    String kind = tag.getString("Kind"), id = tag.getString("Id"), name = tag.getString("Name");
    if (kind.isEmpty() || kind.length() > MAX_KIND_LENGTH || id.length() > maxLength
        || name.length() > maxLength) return null;
    return new FilterTarget(kind, id, name);
  }

  public static ListTag saveAll(Collection<FilterTarget> targets) {
    var list = new ListTag();
    targets.forEach(target -> list.add(save(target)));
    return list;
  }

  /** Adds up to {@code maxEntries} well-formed entries that {@code accept} allows. */
  public static void loadAll(ListTag list, int maxEntries, int maxLength,
      Predicate<FilterTarget> accept, Collection<FilterTarget> into) {
    for (int i = 0; i < Math.min(maxEntries, list.size()); i++) {
      var target = load(list.getCompound(i), maxLength);
      if (target != null && accept.test(target)) into.add(target);
    }
  }
}
