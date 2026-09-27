package com.zeromods.core.client.filter;

import com.zeromods.core.filter.FilterTarget;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

/** Names and icons for the standard {@link FilterTarget} kinds; {@code null} for other kinds. */
public final class StandardTargets {
  private StandardTargets() {}

  public static String name(FilterTarget target) {
    return switch (target.kind()) {
      case FilterTarget.PLAYER -> target.name().isEmpty() ? target.id() : target.name();
      case FilterTarget.INDIVIDUAL -> entityName(target.name());
      case FilterTarget.MOB -> target.id().startsWith("#") ? target.id() : entityName(target.id());
      case FilterTarget.ITEM -> {
        var id = ResourceLocation.tryParse(target.id());
        yield target.id().startsWith("#") || id == null
            ? target.id() : BuiltInRegistries.ITEM.get(id).getDescription().getString();
      }
      default -> null;
    };
  }

  public static ItemStack icon(FilterTarget target) {
    return switch (target.kind()) {
      case FilterTarget.PLAYER -> new ItemStack(Items.PLAYER_HEAD);
      case FilterTarget.INDIVIDUAL -> new ItemStack(Items.NAME_TAG);
      case FilterTarget.MOB, FilterTarget.ITEM -> {
        if (target.id().startsWith("#")) yield new ItemStack(Items.NAME_TAG);
        var id = ResourceLocation.tryParse(target.id());
        if (id == null) yield new ItemStack(Items.BARRIER);
        if (target.kind().equals(FilterTarget.ITEM)) yield new ItemStack(BuiltInRegistries.ITEM.get(id));
        var egg = SpawnEggItem.byId(BuiltInRegistries.ENTITY_TYPE.get(id));
        yield new ItemStack(egg == null ? Items.PAPER : egg);
      }
      default -> null;
    };
  }

  private static String entityName(String typeId) {
    var id = ResourceLocation.tryParse(typeId);
    return id == null ? typeId : BuiltInRegistries.ENTITY_TYPE.get(id).getDescription().getString();
  }
}
