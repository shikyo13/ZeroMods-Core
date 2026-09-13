package com.zeromods.core.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
/** Client-only scaled block/item previews for tutorials and machine screens. */
public final class ModelPreview {
    private ModelPreview() {}
    public static void item(GuiGraphics graphics, ItemStack stack, double centerX, double centerY, double size) {
        if (!Double.isFinite(size) || size <= 0) return;
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(centerX-size/2,centerY-size/2,0);
            graphics.pose().scale((float)size/16,(float)size/16,1);
            graphics.renderItem(stack,0,0);
        } finally { graphics.pose().popPose(); }
    }
}
