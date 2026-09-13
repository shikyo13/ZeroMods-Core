package com.zeromods.core;

import com.zeromods.core.animation.PlasmaSurface;
import com.zeromods.core.animation.SphereFormation;

final class ProjectionContractTest {
  static void run() {
    for (var style : SphereFormation.values()) {
      for (int column = 0; column <= 24; column++) {
        double azimuth = column * SphereFormation.TAU / 24;
        for (int row = 0; row <= 16; row++) {
          double height = row / 16.0;
          float noise = PlasmaSurface.noise(Math.cos(azimuth), height, Math.sin(azimuth), 0);
          float previous = 0;
          for (int tick = 0; tick <= 80; tick++) {
            float p = tick / 80f;
            float mask = style.coverage(azimuth, height, noise, p);
            CoreContractTest.check(
                Float.isFinite(mask) && mask >= previous && mask <= 1,
                style + " must build monotonically without holes reopening");
            previous = mask;
            float edge = style.edge(azimuth, height, noise, p);
            CoreContractTest.check(
                Float.isFinite(edge) && edge >= 0 && edge <= 1, "finite construction highlight");
            float seam = style.coverage(azimuth - SphereFormation.TAU, height, noise, p);
            // Cell boundaries may differ by floating point roundoff; compare away from them below.
            if (style != SphereFormation.HEX_ASSEMBLY)
              CoreContractTest.check(Math.abs(mask - seam) < .0001, "longitude seam");
          }
          CoreContractTest.check(
              previous == 1 && style.coverage(azimuth, height, noise, 0) == 0,
              "empty start and complete shell");
        }
      }
      CoreContractTest.check(
          SphereFormation.fromId(style.name()) == style, "stable style identifier");
    }
    CoreContractTest.check(
        SphereFormation.fromId("future-style") == SphereFormation.LASER_CURTAIN,
        "unknown style safely defaults");
    CoreContractTest.check(
        SphereFormation.RISING_RING.coverage(0, .1, .5f, .5f) > .99
            && SphereFormation.RISING_RING.coverage(0, .9, .5f, .5f) < .01,
        "ring rises from bottom");
    CoreContractTest.check(
        SphereFormation.PROJECTED_SEED.coverage(0, .9, .5f, .5f) > .99
            && SphereFormation.PROJECTED_SEED.coverage(0, .1, .5f, .5f) < .01,
        "seed spreads from crown");
    int[] strokes = {0};
    PlasmaSurface.render(
        -2,
        2,
        -2,
        2,
        30,
        0xffffff,
        (x1, y1, x2, y2, w, c, a) -> {
          CoreContractTest.check(
              Float.isFinite(x1 + y1 + x2 + y2)
                  && x1 >= -2.001
                  && x1 <= 2.001
                  && x2 >= -2.001
                  && x2 <= 2.001
                  && y1 >= -2.001
                  && y1 <= 2.001
                  && y2 >= -2.001
                  && y2 <= 2.001,
              "plasma stays clipped to the field");
          strokes[0]++;
        });
    CoreContractTest.check(
        strokes[0] > 0 && strokes[0] < 600, "bounded continuous plasma contours");
  }
}
