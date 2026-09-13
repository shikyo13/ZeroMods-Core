package com.zeromods.core.animation;

/** Smooth scalar field and interpolated contours, rather than opaque grid tiles. */
public final class PlasmaSurface {
  private static final float CELL = .25f, LEVEL = .5f;
  private static final float HALO_WIDTH = .16f, GLOW_WIDTH = .065f, CORE_WIDTH = .014f;

  private PlasmaSurface() {}

  public static float noise(double x, double y, double z, double seconds) {
    x = x * .85 + seconds * .13;
    y = y * .85 - seconds * .08;
    z = z * .85 + seconds * .1;
    return (float) ((Math.sin(x * 2.1 + Math.sin(z * 1.8))
        + Math.sin(y * 2.7 + z * .9 + Math.sin(x * 1.3))) * .25 + .5);
  }

  public static void render(
      float left,
      float right,
      float bottom,
      float top,
      float ticks,
      int color,
      HexFieldPattern.Stroke stroke) {
    for (int x = (int) Math.floor(left / CELL); x < Math.ceil(right / CELL); x++) {
      float a = Math.max(left, x * CELL), b = Math.min(right, (x + 1) * CELL);
      for (int y = (int) Math.floor(bottom / CELL); y < Math.ceil(top / CELL); y++) {
        float c = Math.max(bottom, y * CELL), d = Math.min(top, (y + 1) * CELL);
        float ac = surfaceNoise(a, c, ticks), bc = surfaceNoise(b, c, ticks);
        float bd = surfaceNoise(b, d, ticks), ad = surfaceNoise(a, d, ticks);
        contour(a, c, ac, b, c, bc, b, d, bd, color, stroke);
        contour(a, c, ac, b, d, bd, a, d, ad, color, stroke);
      }
    }
  }

  /**
   * Broad, interpolated light beneath the contour cores; world coordinates keep rail seams aligned.
   */
  public static void glow(
      float left,
      float right,
      float bottom,
      float top,
      float ticks,
      int color,
      PlanarProjection.Patch patch) {
    for (int x = (int) Math.floor(left / CELL); x < Math.ceil(right / CELL); x++) {
      float a = Math.max(left, x * CELL), b = Math.min(right, (x + 1) * CELL);
      for (int y = (int) Math.floor(bottom / CELL); y < Math.ceil(top / CELL); y++) {
        float c = Math.max(bottom, y * CELL), d = Math.min(top, (y + 1) * CELL);
        patch.draw(
            a,
            c,
            b,
            d,
            color,
            glowAlpha(a, c, ticks),
            glowAlpha(b, c, ticks),
            glowAlpha(b, d, ticks),
            glowAlpha(a, d, ticks));
      }
    }
  }

  private static float surfaceNoise(float x, float y, float ticks) {
    return noise(x, y, 0, ticks / 20.0);
  }

  public static float glowAlpha(float x, float y, float ticks) {
    float distance = Math.abs(surfaceNoise(x, y, ticks) - LEVEL);
    return .025f + .16f * (float) Math.exp(-distance * 18);
  }

  private static void contour(
      float ax,
      float ay,
      float an,
      float bx,
      float by,
      float bn,
      float cx,
      float cy,
      float cn,
      int color,
      HexFieldPattern.Stroke stroke) {
    if ((an < LEVEL) == (bn < LEVEL) && (bn < LEVEL) == (cn < LEVEL)) return;
    // Rotate the triangle so the first vertex is alone on its side of the contour.
    if ((an < LEVEL) == (bn < LEVEL)) {
      contour(cx, cy, cn, ax, ay, an, bx, by, bn, color, stroke);
      return;
    }
    if ((an < LEVEL) == (cn < LEVEL)) {
      contour(bx, by, bn, cx, cy, cn, ax, ay, an, color, stroke);
      return;
    }
    float ab = (LEVEL - an) / (bn - an), ac = (LEVEL - an) / (cn - an);
    float x1 = ax + (bx - ax) * ab, y1 = ay + (by - ay) * ab;
    float x2 = ax + (cx - ax) * ac, y2 = ay + (cy - ay) * ac;
    stroke.draw(x1, y1, x2, y2, HALO_WIDTH, color, .035f);
    stroke.draw(x1, y1, x2, y2, GLOW_WIDTH, color, .12f);
    stroke.draw(x1, y1, x2, y2, CORE_WIDTH, color, .58f);
  }
}
