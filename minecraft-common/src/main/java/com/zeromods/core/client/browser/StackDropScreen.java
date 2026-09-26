package com.zeromods.core.client.browser;

import java.util.List;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

/**
 * A screen with areas that accept stacks dragged from JEI or EMI. Register the screen class with
 * {@link RecipeBrowsers#register}; Core's browser plugins serve it, so a mod needs no browser code.
 */
public interface StackDropScreen {
  /** Areas that accept this stack, in screen coordinates. Empty when the stack is not accepted. */
  List<StackDropTarget> dropTargets(ItemStack stack);

  /** The screen's panel, in screen coordinates, which a browser overlay should keep clear of. */
  Rect2i browserExclusion();

  /** Whether the browser overlay should currently be shown and receive input on this screen. */
  default boolean showsBrowser() {
    return true;
  }
}
