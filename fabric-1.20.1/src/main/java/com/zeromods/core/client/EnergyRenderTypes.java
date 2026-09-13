package com.zeromods.core.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;

public final class EnergyRenderTypes extends RenderType {
  private EnergyRenderTypes() {
    super("zeromodscore_energy", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 4096, false, true, () -> {}, () -> {});
  }

  private static ShaderInstance surfaceShader;
  public static void surfaceShader(ShaderInstance shader) { surfaceShader = shader; }
  public static final RenderType SURFACE = RenderType.create("zeromodscore_surface",
      DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 4096, false, true,
      RenderType.CompositeState.builder()
          .setShaderState(new ShaderStateShard(() -> surfaceShader))
          .setTransparencyState(ADDITIVE_TRANSPARENCY)
          .setDepthTestState(LEQUAL_DEPTH_TEST).setCullState(NO_CULL)
          .setWriteMaskState(COLOR_WRITE).createCompositeState(false));

  public static RenderType translucent(String name, ResourceLocation texture) {
    return
      create(
          name,
          DefaultVertexFormat.NEW_ENTITY,
          VertexFormat.Mode.QUADS,
          4096,
          false,
          true,
          CompositeState.builder()
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
