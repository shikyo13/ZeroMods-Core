package com.zeromods.core.client.filter;

import com.zeromods.core.filter.FilterTarget;
import com.zeromods.core.filter.TargetExceptions;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * Two paged exception lists side by side: the allowing list on the left, the denying list on the
 * right. The host screen registers the buttons, draws the panel and forwards scrolling.
 */
public final class ExceptionListPanel {
  public static final int HEIGHT = 83, ROW_HEIGHT = 19, ROWS = 3, GAP = 6;
  private final TargetExceptions exceptions;
  private final ExceptionListStyle style;
  private final int x, y, width;
  private final Consumer<Boolean> add;
  private final Runnable changed;
  private final int[] pages;

  /**
   * @param pages two page indexes, one per list (included first); kept by the host across rebuilds
   * @param add opens the host's entry screen for the included ({@code false}) or excluded list
   */
  public ExceptionListPanel(TargetExceptions exceptions, ExceptionListStyle style, int x, int y,
      int width, int[] pages, Consumer<Boolean> add, Runnable changed) {
    this.exceptions = exceptions; this.style = style; this.x = x; this.y = y;
    this.width = width; this.pages = pages; this.add = add; this.changed = changed;
  }

  public int top() { return y; }

  public int columnWidth() { return (width - GAP) / 2; }

  public boolean denies(boolean exclude) { return style.denies(exclude); }

  public int columnX(boolean exclude) { return x + (style.denies(exclude) ? columnWidth() + GAP : 0); }

  public String heading(boolean exclude) { return style.heading(exclude); }

  private Set<FilterTarget> entries(boolean exclude) { return exceptions.list(exclude); }

  private boolean[] leftToRight() {
    return style.denies(false) ? new boolean[] {true, false} : new boolean[] {false, true};
  }

  private int pageCount(boolean exclude) {
    return Math.max(1, (entries(exclude).size() + ROWS - 1) / ROWS);
  }

  public void widgets(Consumer<AbstractWidget> register) {
    for (boolean exclude : leftToRight()) {
      int col = exclude ? 1 : 0, cx = columnX(exclude), cw = columnWidth();
      pages[col] = Math.min(pages[col], pageCount(exclude) - 1);
      var plus = style.button(cx + cw - 20, y, 20, 18, "+", () -> add.accept(exclude));
      plus.setTooltip(Tooltip.create(Component.literal(style.addTooltip(exclude))));
      register.accept(plus);
      var list = new ArrayList<>(entries(exclude));
      for (int i = 0; i < ROWS && pages[col] * ROWS + i < list.size(); i++) {
        var target = list.get(pages[col] * ROWS + i);
        var remove = style.button(cx + cw - 19, y + 21 + i * ROW_HEIGHT, 17, 17, "×", () -> {
          entries(exclude).remove(target);
          changed.run();
        });
        remove.setTooltip(Tooltip.create(Component.literal(style.removeTooltip(target))));
        register.accept(remove);
      }
    }
  }

  /** Which list the point is over, or {@code null} outside both. */
  public Boolean sideAt(double mx, double my) {
    if (my < y + 20 || my >= y + HEIGHT) return null;
    for (boolean exclude : new boolean[] {false, true})
      if (mx >= columnX(exclude) && mx < columnX(exclude) + columnWidth()) return exclude;
    return null;
  }

  public boolean scroll(double mx, double my, double delta) {
    Boolean side = sideAt(mx, my);
    if (side == null || delta == 0) return false;
    int col = side ? 1 : 0;
    pages[col] = Math.max(0, Math.min(pageCount(side) - 1, pages[col] + (delta < 0 ? 1 : -1)));
    changed.run();
    return true;
  }

  public void render(GuiGraphics g, int mx, int my) {
    var font = Minecraft.getInstance().font;
    var theme = style.theme();
    for (boolean exclude : leftToRight()) {
      int cx = columnX(exclude), cw = columnWidth(), col = exclude ? 1 : 0;
      int color = style.denies(exclude) ? theme.error() : theme.success();
      g.drawString(font, style.heading(exclude), cx + 3, y + 5, color, false);
      if (entries(exclude).size() > ROWS) {
        String count = (pages[col] + 1) + "/" + pageCount(exclude);
        g.drawString(font, count, cx + cw - 25 - font.width(count), y + 5, color, false);
      }
      g.fill(cx, y + 20, cx + cw, y + HEIGHT, color);
      g.fill(cx + 1, y + 21, cx + cw - 1, y + HEIGHT - 1, theme.panel());
      var list = new ArrayList<>(entries(exclude));
      if (list.isEmpty()) {
        var lines = font.split(Component.literal(style.emptyHint()), cw - 12);
        for (int i = 0; i < lines.size(); i++)
          g.drawString(font, lines.get(i), cx + 6, y + 31 + i * 11, theme.muted(), false);
      }
      for (int i = 0; i < ROWS && pages[col] * ROWS + i < list.size(); i++) {
        var target = list.get(pages[col] * ROWS + i);
        int ry = y + 22 + i * ROW_HEIGHT;
        g.renderItem(style.icon(target), cx + 3, ry);
        g.drawString(font, font.plainSubstrByWidth(style.name(target), cw - 44), cx + 22, ry + 4,
            theme.text(), false);
      }
    }
  }

  /** Shows an entry's full name and ID while the pointer is over it. */
  public void tooltip(GuiGraphics g, int mx, int my) {
    Boolean side = sideAt(mx, my);
    if (side == null) return;
    int cw = columnWidth(), cx = columnX(side);
    if (mx >= cx + cw - 20) return;
    var list = new ArrayList<>(entries(side));
    int index = pages[side ? 1 : 0] * ROWS + (my - y - 21) / ROW_HEIGHT;
    if (index >= 0 && index < list.size()) {
      var target = list.get(index);
      g.renderTooltip(Minecraft.getInstance().font,
          Component.literal(style.name(target) + "\n" + target.id()), mx, my);
    }
  }
}
