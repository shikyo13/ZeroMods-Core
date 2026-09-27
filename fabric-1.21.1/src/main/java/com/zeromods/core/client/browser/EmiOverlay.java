package com.zeromods.core.client.browser;

import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** EMI 1.1.24 attaches itself to container screens only; forward other drop screens explicitly. */
final class EmiOverlay implements RecipeBrowsers.Overlay {
  @Override
  public void init(Screen screen) {
    EmiScreenManager.addWidgets(screen);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    graphics.pose().pushPose();
    var context = EmiDrawContext.wrap(graphics);
    EmiScreenManager.drawBackground(context, mouseX, mouseY, partialTick);
    EmiScreenManager.render(context, mouseX, mouseY, partialTick);
    EmiScreenManager.drawForeground(context, mouseX, mouseY, partialTick);
    graphics.pose().popPose();
  }

  @Override
  public boolean mouseClicked(double x, double y, int button) {
    return EmiScreenManager.mouseClicked(x, y, button);
  }

  @Override
  public boolean mouseReleased(double x, double y, int button) {
    return EmiScreenManager.mouseReleased(x, y, button);
  }

  @Override
  public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
    return EmiScreenManager.mouseDragged(x, y, button, dx, dy);
  }

  @Override
  public boolean mouseScrolled(double x, double y, double amount) {
    return EmiScreenManager.mouseScrolled(x, y, amount);
  }

  @Override
  public boolean keyPressed(int key, int scanCode, int modifiers) {
    return EmiScreenManager.keyPressed(key, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char character, int modifiers) {
    return EmiScreenManager.search.charTyped(character, modifiers);
  }
}
