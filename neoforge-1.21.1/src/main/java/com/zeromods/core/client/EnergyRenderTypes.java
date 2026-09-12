package com.zeromods.core.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;

public final class EnergyRenderTypes extends RenderStateShard {
  private EnergyRenderTypes() {
    super("zeromodscore_energy", () -> {}, () -> {});
  }

  public static RenderType translucent(String name, ResourceLocation texture) {
    return
      RenderType.create(
          name,
          DefaultVertexFormat.NEW_ENTITY,
          VertexFormat.Mode.QUADS,
          4096,
          false,
          true,
          RenderType.CompositeState.builder()
              .setShaderState(RENDERTYPE_EYES_SHADER)
              .setTextureState(
                  new TextureStateShard(
                      texture,
                      false,
                      false))
              .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
              .setDepthTestState(LEQUAL_DEPTH_TEST)
              .setCullState(NO_CULL)
              .setWriteMaskState(COLOR_WRITE)
              .createCompositeState(false));
  }
}
