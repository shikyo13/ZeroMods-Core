package com.zeromods.core.animation;

/** Smooth scalar field and interpolated contours, rather than opaque grid tiles. */
public final class PlasmaSurface {
  private static final float CELL = .35f, LEVEL = .5f;

  private PlasmaSurface() {}

  public static float noise(double x, double y, double z, double seconds) {
    return (float)
        (.5
            + .23 * Math.sin(x * 2.1 + Math.sin(z * 1.8) + seconds * .16)
            + .19 * Math.sin(y * 2.7 + z * .9 + Math.sin(x * 1.3) - seconds * .12));
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
        float ac = noise(a, c, 0, ticks / 20), bc = noise(b, c, 0, ticks / 20);
        float bd = noise(b, d, 0, ticks / 20), ad = noise(a, d, 0, ticks / 20);
        contour(a, c, ac, b, c, bc, b, d, bd, color, stroke);
        contour(a, c, ac, b, d, bd, a, d, ad, color, stroke);
      }
    }
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
    stroke.draw(x1, y1, x2, y2, .055f, color, .055f);
    stroke.draw(x1, y1, x2, y2, .012f, color, .34f);
  }
}
