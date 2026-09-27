package com.zeromods.core.client.filter;

import com.zeromods.core.filter.FilterTarget;
import com.zeromods.core.ui.UiTheme;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.world.item.ItemStack;

/** What a mod supplies to show its exception lists: wording, colors, names, icons and buttons. */
public interface ExceptionListStyle {
  UiTheme theme();

  String heading(boolean exclude);

  /** Whether this list stops or harms what it names; it is drawn in the error color, on the right. */
  boolean denies(boolean exclude);

  String name(FilterTarget target);

  ItemStack icon(FilterTarget target);

  String emptyHint();

  String addTooltip(boolean exclude);

  String removeTooltip(FilterTarget target);

  AbstractButton button(int x, int y, int width, int height, String label, Runnable press);
}
