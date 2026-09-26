package com.zeromods.core.client.browser;

import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

/** Serves every registered {@link StackDropScreen} to JEI; ghost ingredients never move inventory. */
@JeiPlugin
public final class CoreJeiPlugin implements IModPlugin {
  @Override
  public ResourceLocation getPluginUid() {
    return ResourceLocation.fromNamespaceAndPath("zeromodscore", "stack_drops");
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    // With EMI installed, EMI serves the drops and draws its own overlay.
    if (net.neoforged.fml.ModList.get().isLoaded("emi")) return;
    RecipeBrowsers.announce();
    for (var screen : RecipeBrowsers.screens()) register(registration, screen);
  }

  private static <T extends Screen> void register(IGuiHandlerRegistration registration, Class<T> type) {
    registration.addGuiScreenHandler(type, screen -> {
      var drops = (StackDropScreen) screen;
      if (screen.width <= 0 || screen.height <= 0 || !drops.showsBrowser()) return null;
      var area = drops.browserExclusion();
      var window = Minecraft.getInstance().getWindow();
      return new Properties(type, area.getX(), area.getY(), area.getWidth(), area.getHeight(),
          window.getGuiScaledWidth(), window.getGuiScaledHeight());
    });
    registration.addGhostIngredientHandler(type, new IGhostIngredientHandler<T>() {
      @Override
      public <I> List<Target<I>> getTargetsTyped(T screen, ITypedIngredient<I> ingredient, boolean doStart) {
        var stack = ingredient.getItemStack();
        var drops = (StackDropScreen) screen;
        if (stack.isEmpty() || !drops.showsBrowser()) return List.of();
        var dropped = stack.get().copy();
        var targets = new ArrayList<Target<I>>();
        for (var drop : drops.dropTargets(dropped))
          targets.add(new Target<>() {
            @Override
            public Rect2i getArea() {
              return drop.area();
            }

            @Override
            public void accept(I ignored) {
              drop.accept().accept(dropped.copy());
            }
          });
        return targets;
      }

      @Override
      public void onComplete() {}
    });
  }

  private record Properties(Class<? extends Screen> screenClass, int guiLeft, int guiTop, int guiXSize,
      int guiYSize, int screenWidth, int screenHeight) implements IGuiProperties {}
}
