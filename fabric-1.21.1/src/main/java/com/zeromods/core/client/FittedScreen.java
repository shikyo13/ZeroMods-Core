package com.zeromods.core.client;
import com.zeromods.core.ui.CanvasFit;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Keeps the complete control canvas visible, with input mapped to the same canvas. */
public abstract class FittedScreen extends Screen {
  private double fitScale = 1;

  protected FittedScreen(Component title) {
    super(title);
  }

  protected void fit(int panelWidth, int panelHeight) {
    fit(panelWidth, panelHeight, 0);
  }

  /** Leave room for external UI such as a recipe browser's bottom search bar. */
  protected void fit(int panelWidth, int panelHeight, int bottomInset) {
    int screenWidth = minecraft.getWindow().getGuiScaledWidth();
    int screenHeight = Math.max(1, minecraft.getWindow().getGuiScaledHeight() - Math.max(0, bottomInset));
    fitScale = CanvasFit.fit(screenWidth, screenHeight, panelWidth, panelHeight, 4).scale();
    width = (int) (screenWidth / fitScale);
    height = (int) (screenHeight / fitScale);
  }

  protected int fitMouse(int coordinate) {
    return (int) (coordinate / fitScale);
  }

  protected void beginFit(GuiGraphics graphics) {
    graphics.pose().pushPose();
    graphics.pose().scale((float) fitScale, (float) fitScale, 1);
  }

  /** GuiGraphics scissor coordinates bypass the pose stack, so apply the canvas transform explicitly. */
  protected void fitScissor(GuiGraphics graphics, int left, int top, int right, int bottom) {
    graphics.enableScissor((int)Math.floor(left * fitScale), (int)Math.floor(top * fitScale),
        (int)Math.ceil(right * fitScale), (int)Math.ceil(bottom * fitScale));
  }

  @Override
  public boolean mouseClicked(double x, double y, int b) {
    return super.mouseClicked(x / fitScale, y / fitScale, b);
  }

  @Override
  public boolean mouseReleased(double x, double y, int b) {
    return super.mouseReleased(x / fitScale, y / fitScale, b);
  }

  @Override
  public boolean mouseDragged(double x, double y, int b, double dx, double dy) {
    return super.mouseDragged(x / fitScale, y / fitScale, b, dx / fitScale, dy / fitScale);
  }

  @Override
  public boolean mouseScrolled(double x, double y, double dx, double dy) {
    return super.mouseScrolled(x / fitScale, y / fitScale, dx, dy);
  }

  @Override
  public void mouseMoved(double x, double y) {
    super.mouseMoved(x / fitScale, y / fitScale);
  }
}
