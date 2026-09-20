package com.zeromods.core.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Frame-local overflow text, shared by all Minecraft adapters. */
final class TextOverflow {
  private static final int TOOLTIP_MAX_WIDTH = 320;
  private static final int TOOLTIP_MARGIN = 12;
  private record Overflow(Component text, int x, int y, int width, int height) {}
  private final List<Overflow> overflow = new ArrayList<>();

  void clear() { overflow.clear(); }

  void drawLabel(Font font, GuiGraphics graphics, String text, int x, int y, int width, int color) {
    String visible = text;
    if (font.width(text) > width) {
      visible = font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…";
      overflow.add(new Overflow(Component.literal(text), x, y, width, font.lineHeight));
    }
    graphics.drawString(font, visible, x, y, color, false);
  }

  void drawParagraph(Font font, GuiGraphics graphics, Component text, int x, int y,
      int width, int height, int color) {
    var lines = font.split(text, width);
    int limit = Math.max(1, height / font.lineHeight);
    boolean clipped = lines.size() > limit;
    for (int i = 0; i < Math.min(limit, lines.size()); i++) {
      if (clipped && i == limit - 1) {
        // Leave space for an ellipsis without cutting a multibyte glyph in half.
        var line = new StringBuilder();
        lines.get(i).accept((index, style, codepoint) -> { line.appendCodePoint(codepoint); return true; });
        String visible = font.plainSubstrByWidth(line.toString(), Math.max(0, width - font.width("…"))) + "…";
        graphics.drawString(font, visible, x, y + i * font.lineHeight, color, false);
      } else graphics.drawString(font, lines.get(i), x, y + i * font.lineHeight, color, false);
    }
    if (clipped) overflow.add(new Overflow(text, x, y, width, Math.min(height, limit * font.lineHeight)));
  }

  void render(Font font, GuiGraphics graphics, int width, int mouseX, int mouseY) {
    for (var label : overflow) {
      if (mouseX >= label.x && mouseX < label.x + label.width
          && mouseY >= label.y && mouseY < label.y + label.height) {
        int tooltipWidth = Math.min(TOOLTIP_MAX_WIDTH, Math.max(1, Math.min(width, graphics.guiWidth()) - 2 * TOOLTIP_MARGIN));
        graphics.renderTooltip(font, font.split(label.text, tooltipWidth), mouseX, mouseY);
        break;
      }
    }
  }
}
