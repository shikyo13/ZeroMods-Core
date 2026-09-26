package com.zeromods.core.client.browser;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

/** Serves every {@link StackDropScreen} to EMI. */
@EmiEntrypoint
public final class CoreEmiPlugin implements EmiPlugin {
  @Override
  public void register(EmiRegistry registry) {
    RecipeBrowsers.announce();
    RecipeBrowsers.overlay(new EmiOverlay());
    registry.addGenericScreenBoundsProvider(screen -> {
      if (!(screen instanceof StackDropScreen drops) || !drops.showsBrowser()
          || screen.width <= 0 || screen.height <= 0) return Bounds.EMPTY;
      var area = drops.browserExclusion();
      return new Bounds(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    });
    registry.addGenericDragDropHandler(new EmiDragDropHandler<Screen>() {
      @Override
      public boolean dropStack(Screen screen, EmiIngredient ingredient, int x, int y) {
        for (var stack : stacks(ingredient))
          for (var drop : targets(screen, stack))
            if (drop.area().contains(x, y)) {
              drop.accept().accept(stack.copy());
              return true;
            }
        return false;
      }

      @Override
      public void render(Screen screen, EmiIngredient ingredient, GuiGraphics graphics,
          int mouseX, int mouseY, float delta) {
        for (var stack : stacks(ingredient)) {
          var drops = targets(screen, stack);
          if (drops.isEmpty()) continue;
          for (var drop : drops) {
            var area = drop.area();
            graphics.fill(area.getX(), area.getY(), area.getX() + area.getWidth(),
                area.getY() + area.getHeight(), drop.highlight());
          }
          return;
        }
      }
    });
  }

  private static List<ItemStack> stacks(EmiIngredient ingredient) {
    return ingredient.getEmiStacks().stream().map(stack -> stack.getItemStack())
        .filter(stack -> !stack.isEmpty()).toList();
  }

  private static List<StackDropTarget> targets(Screen screen, ItemStack stack) {
    return screen instanceof StackDropScreen drops && drops.showsBrowser()
        ? drops.dropTargets(stack.copy()) : List.of();
  }
}
