package com.zeromods.core;

import com.zeromods.core.animation.PlanarProjection;

final class PlanarProjectionTest {
  static void run() {
    var frames =
        new PlanarProjection.Frame[] {
          new PlanarProjection.Frame(0, 12, 0, 5, 0, 2.5f, false),
          new PlanarProjection.Frame(0, 12, 0, 5, 12, 2.5f, false),
          new PlanarProjection.Frame(0, 5, 0, 12, 2.5f, 0, false),
          new PlanarProjection.Frame(0, 5, 0, 12, 2.5f, 12, false),
          new PlanarProjection.Frame(0, 12, 0, 5, 0, 2.5f, true),
          new PlanarProjection.Frame(0, 5, 0, 12, 2.5f, 12, true)
        };
    for (var frame : frames)
      for (int mode = 3; mode < PlanarProjection.PRESET_COUNT; mode++) {
        var style = PlanarProjection.style(mode);
        for (int x = 0; x <= 10; x++)
          for (int y = 0; y <= 10; y++) {
            float u = frame.left() + (frame.right() - frame.left()) * x / 10;
            float v = frame.bottom() + (frame.top() - frame.bottom()) * y / 10;
            float previous = 0;
            for (int tick = 0; tick <= 80; tick++) {
              float alpha = PlanarProjection.coverage(style, frame, u, v, tick / 80f);
              CoreContractTest.check(
                  Float.isFinite(alpha) && alpha >= previous && alpha <= 1,
                  "monotonic plane reveal");
              previous = alpha;
            }
            CoreContractTest.check(previous == 1, "plane finishes at every edge");
          }
        // Adjacent strips share the same frame: values at the join must match exactly.
        float[] seam = {-1, -1};
        PlanarProjection.Patch patch =
            (l, b, r, t, c, a1, a2, a3, a4) -> {
              for (float alpha : new float[] {a1, a2, a3, a4})
                CoreContractTest.check(
                    Float.isFinite(alpha) && alpha >= 0 && alpha <= 1, "finite patch alpha");
              if (r == 6 && b == 0) seam[0] = a2;
              if (l == 6 && b == 0) seam[1] = a1;
            };
        var shared = new PlanarProjection.Frame(0, 12, 0, 5, 0, 2.5f, false);
        PlanarProjection.fill(style, shared, 0, 6, 0, 5, .45f, 1, 0, patch);
        PlanarProjection.fill(style, shared, 6, 12, 0, 5, .45f, 1, 0, patch);
        CoreContractTest.check(seam[0] >= 0 && seam[0] == seam[1], "shared strip boundary");
        int[] strokes = {0};
        PlanarProjection.guides(
            style,
            frame,
            .4f,
            0,
            (x, y, X, Y, w, c, a) -> {
              CoreContractTest.check(
                  Float.isFinite(x + y + X + Y + w + a) && a >= 0 && a <= 1, "finite guides");
              strokes[0]++;
            });
        CoreContractTest.check(strokes[0] <= 32, "bounded guide geometry");
      }
  }
}
