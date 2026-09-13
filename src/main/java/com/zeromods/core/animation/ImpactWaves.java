package com.zeromods.core.animation;

import java.util.List;

/** Local ripple samples; the surface mesh is drawn once regardless of contact count. */
public final class ImpactWaves implements HexFieldPattern.Ripples {
  public static final ImpactWaves NONE = new ImpactWaves(List.of());
  public record Wave(float u, float v, float age) {}
  private final List<Wave> waves;

  public ImpactWaves(List<Wave> waves) { this.waves = List.copyOf(waves); }

  public static ImpactWaves single(float age, float u, float v) {
    return age >= 0 ? new ImpactWaves(List.of(new Wave(u, v, age))) : NONE;
  }

  public float sample(float u, float v, HexFieldPattern.Style style) {
    float strongest = 0, total = 0;
    for (var wave : waves) {
      if (wave.age < 0 || wave.age >= style.lifetime()) continue;
      float radius = wave.age * style.speed();
      float dx = u - wave.u, dy = v - wave.v;
      float reach = radius + style.bandWidth() * 3;
      if (Math.abs(dx) > reach || Math.abs(dy) > reach) continue;
      float distance = (float) Math.sqrt(dx * dx + dy * dy);
      float band = (distance - radius) / style.bandWidth();
      if (Math.abs(band) > 3) continue;
      float value = (float) Math.exp(-band * band) * (1 - wave.age / style.lifetime());
      strongest = Math.max(strongest, value);
      total += value;
    }
    // Preserve a single crest; overlapping crests brighten smoothly without a whiteout.
    return strongest + (1 - strongest) * (float) (1 - Math.exp(-(total - strongest)));
  }

  public void rings(float left, float right, float bottom, float top, int color,
      HexFieldPattern.Style style, HexFieldPattern.Stroke stroke) {
    final int segments = HexFieldPattern.RING_SEGMENTS;
    for (var wave : waves) {
      if (wave.age < 0 || wave.age >= style.lifetime()) continue;
      float radius = wave.age * style.speed(), fade = 1 - wave.age / style.lifetime();
      if (wave.u + radius < left || wave.u - radius > right
          || wave.v + radius < bottom || wave.v - radius > top) continue;
      for (int k = 0; k < segments; k++) {
        double a = k * Math.PI * 2 / segments, b = (k + 1) * Math.PI * 2 / segments;
        float x1 = wave.u + (float) Math.cos(a) * radius, y1 = wave.v + (float) Math.sin(a) * radius;
        float x2 = wave.u + (float) Math.cos(b) * radius, y2 = wave.v + (float) Math.sin(b) * radius;
        if (Math.max(x1,x2) < left || Math.min(x1,x2) > right
            || Math.max(y1,y2) < bottom || Math.min(y1,y2) > top) continue;
        HexFieldPattern.ringSegment(stroke, x1, y1, x2, y2, color, fade);
      }
    }
  }
}
