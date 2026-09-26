package com.zeromods.core.client.browser;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Drop screens registered by mods, and the recipe browsers that announced themselves at startup. */
public final class RecipeBrowsers {
  /** Height kept free at the bottom of the window for a browser's search bar. */
  public static final int SEARCH_BAR_HEIGHT = 24;

  private static final Set<Class<? extends Screen>> screens = new CopyOnWriteArraySet<>();
  private static volatile boolean present;
  private static volatile Overlay overlay = Overlay.NONE;

  private RecipeBrowsers() {}

  /** Register during client setup, before recipe browsers start. */
  public static <T extends Screen & StackDropScreen> void register(Class<T> screen) {
    screens.add(screen);
  }

  public static Set<Class<? extends Screen>> screens() {
    return Set.copyOf(screens);
  }

  /** Called by a browser plugin when its browser is running. */
  public static void announce() {
    present = true;
  }

  /** Space to leave for a browser's search bar, or zero when no browser is installed. */
  public static int bottomInset() {
    return present ? SEARCH_BAR_HEIGHT : 0;
  }

  /** Set by a browser whose overlay only attaches itself to container screens. */
  public static void overlay(Overlay value) {
    overlay = value;
  }

  public static Overlay overlay() {
    return overlay;
  }

  /** Forwarding for a browser overlay on screens it does not attach to by itself. */
  public interface Overlay {
    Overlay NONE = new Overlay() {};

    default void init(Screen screen) {}

    default void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    default boolean mouseClicked(double x, double y, int button) {
      return false;
    }

    default boolean mouseReleased(double x, double y, int button) {
      return false;
    }

    default boolean mouseDragged(double x, double y, int button, double dx, double dy) {
      return false;
    }

    default boolean mouseScrolled(double x, double y, double amount) {
      return false;
    }

    default boolean keyPressed(int key, int scanCode, int modifiers) {
      return false;
    }

    default boolean charTyped(char character, int modifiers) {
      return false;
    }
  }
}
