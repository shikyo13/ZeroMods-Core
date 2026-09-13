package com.zeromods.core.animation;

/** World-anchored hex lattice and impact rings shared by posts and surface rails. */
public final class HexFieldPattern {
  public static final int RING_SEGMENTS = 64;

  public static void ringSegment(Stroke stroke, float x1, float y1, float x2, float y2,
      int color, float fade) {
    stroke.draw(x1, y1, x2, y2, .14f, color, fade * .12f);
    stroke.draw(x1, y1, x2, y2, .020f, color, fade * .60f);
  }

  @FunctionalInterface
  public interface Stroke {
    void draw(float x1, float y1, float x2, float y2, float width, int color, float alpha);
  }

  public interface Ripples {
    float sample(float u, float v, Style style);
    void rings(float left, float right, float bottom, float top, int color, Style style, Stroke stroke);
  }

  public record Style(float lifetime, float speed, float bandWidth, int impactColor, float opacity) {
    public static final Style FIELD = new Style(32, .115f, .26f, 0xD0F7FF, 1);
    public static Style forAccent(int color) {
      int highlight=EnergyColors.highlight(color,.65f);
      return new Style(FIELD.lifetime, FIELD.speed, FIELD.bandWidth, highlight, FIELD.opacity);
    }
    public Style {
      if (!Float.isFinite(lifetime) || !Float.isFinite(speed) || !Float.isFinite(bandWidth) || !Float.isFinite(opacity)
          || lifetime <= 0 || speed < 0 || bandWidth <= 0 || opacity < 0 || opacity > 1) throw new IllegalArgumentException("Invalid field style");
    }
  }
  public static void render(float left, float right, float bottom, float top, float time,
      float impactAge, float impactU, float impactV, int color, Stroke stroke) {
    render(left, right, bottom, top, time, impactAge, impactU, impactV, color, Style.FIELD, stroke);
  }
  public static void render(
      float left,
      float right,
      float bottom,
      float top,
      float time,
      float impactAge,
      float impactU,
      float impactV,
      int color,
      Style style, Stroke output) {
    render(left, right, bottom, top, time, ImpactWaves.single(impactAge, impactU, impactV), color, style, output);
  }

  public static void render(float left, float right, float bottom, float top, float time,
      ImpactWaves waves, int color, Style style, Stroke output) {
    render(left, right, bottom, top, time, (Ripples) waves, color, style, output);
  }

  public static void render(float left, float right, float bottom, float top, float time,
      Ripples waves, int color, Style style, Stroke output) {
    render(left, right, bottom, top, time, waves, color, color, style, output);
  }

  public static void render(float left, float right, float bottom, float top, float time,
      Ripples waves, int color, int accent, Style style, Stroke output) {
    Stroke stroke = (x1,y1,x2,y2,w,c,a) -> output.draw(x1,y1,x2,y2,w,c,a * style.opacity());
    for (int col = (int) Math.floor(left / .45f) - 1; col <= Math.ceil(right / .45f) + 1; col++)
      for (int row = (int) Math.floor(bottom / .5196f) - 1;
          row <= Math.ceil(top / .5196f) + 1;
          row++) {
        float cx = col * .45f, cy = row * .5196f + Math.floorMod(col, 2) * .2598f;
        float scan =
            (float) Math.pow(Math.max(0, Math.cos(cx * .45f - time * .055f + cy * .32f)), 18);
        float wave = waves.sample(cx, cy, style);
        for (int k = 0; k < 6; k++) {
          double a = k * Math.PI / 3, b = (k + 1) * Math.PI / 3;
          stroke.draw(
              cx + (float) Math.cos(a) * .30f,
              cy + (float) Math.sin(a) * .30f,
              cx + (float) Math.cos(b) * .30f,
              cy + (float) Math.sin(b) * .30f,
              .008f + wave * .012f,
              wave > .3 ? style.impactColor() : color,
              .085f + .075f * scan + .72f * wave);
        }
        if (wave > .1f)
          for (int k = 0; k < 3; k++) {
            double a = k * Math.PI / 3, b = (k + 3) * Math.PI / 3;
            stroke.draw(
                cx + (float) Math.cos(a) * .27f,
                cy + (float) Math.sin(a) * .27f,
                cx + (float) Math.cos(b) * .27f,
                cy + (float) Math.sin(b) * .27f,
                .028f,
                color,
                wave * .13f);
          }
      }
    waves.rings(left, right, bottom, top, accent, style, stroke);
  }
}
