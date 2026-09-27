package com.zeromods.core.client;
import com.zeromods.core.client.browser.RecipeBrowsers;
import com.zeromods.core.client.browser.StackDropScreen;
import com.zeromods.core.ui.CanvasFit;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

/** Keeps the complete control canvas visible, with input mapped to the same canvas. */
public abstract class FittedScreen extends Screen {
  private double fitScale = 1;
  private final TextOverflow overflow = new TextOverflow();
  private boolean browserPending = true;
  private int browserMouseX, browserMouseY;
  private float browserPartialTick;

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

  /** A canvas rectangle in screen coordinates, kept inside the drawn area. */
  protected Rect2i screenRect(int x, int y, int width, int height) {
    return new Rect2i((int) Math.ceil(x * fitScale), (int) Math.ceil(y * fitScale),
        (int) Math.floor(width * fitScale), (int) Math.floor(height * fitScale));
  }

  private boolean browser() {
    return this instanceof StackDropScreen drops && drops.showsBrowser();
  }

  @Override
  protected void clearWidgets() {
    super.clearWidgets();
    browserPending = true;
  }

  protected void beginFit(GuiGraphics graphics) {
    overflow.clear();
    graphics.pose().pushPose();
    graphics.pose().scale((float) fitScale, (float) fitScale, 1);
  }

  /** Ends the canvas transform started by {@link #beginFit}, then draws a recipe browser overlay. */
  protected void endFit(GuiGraphics graphics) {
    graphics.pose().popPose();
    if (browser()) RecipeBrowsers.overlay().render(graphics, browserMouseX, browserMouseY, browserPartialTick);
  }

  /** GuiGraphics scissor coordinates bypass the pose stack, so apply the canvas transform explicitly. */
  protected void fitScissor(GuiGraphics graphics, int left, int top, int right, int bottom) {
    graphics.enableScissor((int)Math.floor(left * fitScale), (int)Math.floor(top * fitScale),
        (int)Math.ceil(right * fitScale), (int)Math.ceil(bottom * fitScale));
  }

  @Override
  public boolean mouseClicked(double x, double y, int b) {
    if (browser() && RecipeBrowsers.overlay().mouseClicked(x, y, b)) return true;
    return super.mouseClicked(x / fitScale, y / fitScale, b);
  }

  @Override
  public boolean mouseReleased(double x, double y, int b) {
    if (browser() && RecipeBrowsers.overlay().mouseReleased(x, y, b)) return true;
    return super.mouseReleased(x / fitScale, y / fitScale, b);
  }

  @Override
  public boolean mouseDragged(double x, double y, int b, double dx, double dy) {
    if (browser() && RecipeBrowsers.overlay().mouseDragged(x, y, b, dx, dy)) return true;
    return super.mouseDragged(x / fitScale, y / fitScale, b, dx / fitScale, dy / fitScale);
  }

  @Override
  public boolean mouseScrolled(double x, double y, double dx, double dy) {
    if (browser() && RecipeBrowsers.overlay().mouseScrolled(x, y, dy)) return true;
    return super.mouseScrolled(x / fitScale, y / fitScale, dx, dy);
  }

  @Override
  public boolean keyPressed(int key, int scanCode, int modifiers) {
    if (browser() && RecipeBrowsers.overlay().keyPressed(key, scanCode, modifiers)) return true;
    return super.keyPressed(key, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char character, int modifiers) {
    if (browser() && RecipeBrowsers.overlay().charTyped(character, modifiers)) return true;
    return super.charTyped(character, modifiers);
  }

  @Override
  public void mouseMoved(double x, double y) {
    super.mouseMoved(x / fitScale, y / fitScale);
  }
  protected void drawLabel(GuiGraphics graphics, String text, int x, int y, int width, int color) {
    overflow.drawLabel(font, graphics, text, x, y, width, color);
  }

  protected void drawParagraph(GuiGraphics graphics, Component text, int x, int y,
      int width, int height, int color) {
    overflow.drawParagraph(font, graphics, text, x, y, width, height, color);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    boolean browser = browser();
    if (browser && browserPending) {
      browserPending = false;
      RecipeBrowsers.overlay().init(this);
    }
    browserMouseX = (int) (mouseX * fitScale);
    browserMouseY = (int) (mouseY * fitScale);
    browserPartialTick = partialTick;
    super.render(graphics, mouseX, mouseY, partialTick);
    overflow.render(font, graphics, width, mouseX, mouseY);
  }
}
