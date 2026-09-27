package com.zeromods.core.neoforge;
import com.zeromods.core.filter.EntityCategories;
import com.zeromods.core.filter.EntitySelection;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import java.util.UUID;
/** Registry-aware adapter shared by barriers, sensors and future entity-filtering machines. */
public record MinecraftEntitySubject(Entity entity) implements EntitySelection.Subject {
    public MinecraftEntitySubject { java.util.Objects.requireNonNull(entity); }
    public UUID identity() { return entity.getUUID(); }
    public boolean spectator() { return entity.isSpectator(); }
    public int category() {
        return entity instanceof Player ? EntityCategories.PLAYER
            : entity instanceof Enemy ? EntityCategories.HOSTILE
            : entity instanceof LivingEntity ? EntityCategories.PASSIVE
            : entity instanceof ItemEntity ? EntityCategories.ITEM
            : entity instanceof Projectile ? EntityCategories.PROJECTILE
            : EntityCategories.NONLIVING;
    }
    public boolean living() { return entity instanceof LivingEntity; }
    public boolean baby() { return entity instanceof LivingEntity living && living.isBaby(); }
    public boolean scoreboardTag(String tag) { return entity.getTags().contains(tag); }
    public boolean entityType(String query) {
        var id=ResourceLocation.tryParse(query.startsWith("#")?query.substring(1):query);
        return id!=null && (query.startsWith("#")?entity.getType().is(TagKey.create(Registries.ENTITY_TYPE,id)):BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(id));
    }
    public boolean itemType(String query) {
        var id=ResourceLocation.tryParse(query.startsWith("#")?query.substring(1):query);
        return entity instanceof ItemEntity item && id!=null && (query.startsWith("#")?item.getItem().is(TagKey.create(Registries.ITEM,id)):BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).equals(id));
    }
}
