package com.zeromods.core.client.browser;

import java.util.function.Consumer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

/**
 * An area that accepts item stacks dragged from a recipe browser, in screen coordinates.
 *
 * @param highlight ARGB colour shown over the area while a stack is dragged
 */
public record StackDropTarget(Rect2i area, int highlight, Consumer<ItemStack> accept) {}
