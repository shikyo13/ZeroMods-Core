package com.zeromods.core.client;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.ShaderInstance;

/** Keeps energy effects emissive when a shader pack draws block entities. */
public final class ShaderPackCompat {
  private static Bridge bridge = findBridge();

  private ShaderPackCompat() {}

  public static ShaderInstance emissiveShader(ShaderInstance fallback) {
    Bridge current = bridge;
    if (current == null) return fallback;
    try {
      Object pipeline = current.pipeline().invoke(current.manager().invoke(null));
      if (!current.pipelineType().isInstance(pipeline)
          || !(boolean) current.override().invoke(pipeline)
          || (boolean) current.shadow().invoke(current.api())) return fallback;
      Object shaders = current.shaderMap().invoke(pipeline);
      ShaderInstance shader = (ShaderInstance) current.shader().invoke(shaders, current.eyes());
      return shader == null ? fallback : shader;
    } catch (ReflectiveOperationException | RuntimeException failure) {
      disable(failure);
      return fallback;
    }
  }

  public static boolean active() {
    Bridge current = bridge;
    if (current == null) return false;
    try {
      Object pipeline = current.pipeline().invoke(current.manager().invoke(null));
      return current.pipelineType().isInstance(pipeline)
          && (boolean) current.override().invoke(pipeline);
    } catch (ReflectiveOperationException | RuntimeException failure) {
      disable(failure);
      return false;
    }
  }

  /**
   * Shader packs brighten emissive colour several times and raise exposure in dark scenes, which
   * turns faint energy fills into opaque panels. Maps a vanilla energy alpha to one that reads the
   * same under a pack: broad low-alpha fills are reduced most, bright strokes least, and both more
   * in darkness. {@code sceneLight} is 0 for an unlit scene and 1 for full daylight.
   */
  public static float emissiveAlpha(float alpha, float sceneLight) {
    float light = Math.max(0, Math.min(1, sceneLight));
    float faint = .10f + .40f * light, bright = .9f;
    return alpha * (faint + (bright - faint) * alpha);
  }

  public static boolean shadowPass() {
    Bridge current = bridge;
    if (current == null) return false;
    try {
      return (boolean) current.shadow().invoke(current.api());
    } catch (ReflectiveOperationException | RuntimeException failure) {
      disable(failure);
      return false;
    }
  }

  private static void disable(Exception failure) {
    bridge = null;
    LogUtils.getLogger().warn("Energy shader integration is unavailable; using normal rendering", failure);
  }

  private static Bridge findBridge() {
    for (String namespace : new String[] {"net.irisshaders.iris", "net.coderbot.iris"}) {
      try {
        Class<?> iris = Class.forName(namespace + ".Iris");
        Class<?> apiType = Class.forName(namespace + ".api.v0.IrisApi");
        Class<?> pipeline = Class.forName(namespace + ".pipeline.ShaderRenderingPipeline");
        Class<?> key = Class.forName(namespace + ".pipeline.programs.ShaderKey");
        Method manager = iris.getMethod("getPipelineManager");
        Method shaderMap = pipeline.getMethod("getShaderMap");
        return new Bridge(manager, manager.getReturnType().getMethod("getPipelineNullable"),
            pipeline, pipeline.getMethod("shouldOverrideShaders"), shaderMap,
            shaderMap.getReturnType().getMethod("getShader", key), key.getField("ENTITIES_EYES").get(null),
            apiType.getMethod("getInstance").invoke(null), apiType.getMethod("isRenderingShadowPass"));
      } catch (ClassNotFoundException absent) {
        // Iris and Oculus are optional.
      } catch (ReflectiveOperationException | LinkageError incompatible) {
        LogUtils.getLogger().warn("Unsupported energy shader integration for {}", namespace, incompatible);
      }
    }
    return null;
  }

  private record Bridge(Method manager, Method pipeline, Class<?> pipelineType, Method override,
      Method shaderMap, Method shader, Object eyes, Object api, Method shadow) {}
}
