package com.zeromods.core.animation;

/** Geometry-independent reveal for a rectangular field in world-plane coordinates. */
public final class PlanarProjection {
  public static final int FIRST_PRESET = 3;
  public static final int PRESET_COUNT = FIRST_PRESET + SphereFormation.values().length;
  public static final int DURATION_TICKS = 80;
  private static final float CELL_SIZE = .5f, DISSOLVE_CELL_SIZE = .25f, FRONT_WIDTH = .045f;
  private static final int FAN_RAYS = 12, RIBS = 8;

  public record Frame(
      float left,
      float right,
      float bottom,
      float top,
      float sourceX,
      float sourceY,
      boolean horizontal) {
    public Frame {
      if (!Float.isFinite(left + right + bottom + top + sourceX + sourceY)
          || right <= left
          || top <= bottom) throw new IllegalArgumentException("Invalid field frame");
    }

    public float x(float x) {
      return (x - left) / (right - left);
    }

    public float y(float y) {
      return (y - bottom) / (top - bottom);
    }

    public boolean alongX() {
      return Math.min(Math.abs(sourceX - left), Math.abs(sourceX - right)) < .01f;
    }

    public float travel(float x, float y) {
      return alongX()
          ? (sourceX < (left + right) / 2 ? x(x) : 1 - x(x))
          : (sourceY < (bottom + top) / 2 ? y(y) : 1 - y(y));
    }

    public float targetX(float progress) {
      return alongX()
          ? sourceX + ((sourceX < (left + right) / 2 ? right : left) - sourceX) * progress
          : sourceX;
    }

    public float targetY(float progress) {
      return alongX()
          ? sourceY
          : sourceY + ((sourceY < (bottom + top) / 2 ? top : bottom) - sourceY) * progress;
    }
  }

  @FunctionalInterface
  public interface Patch {
    void draw(
        float left,
        float bottom,
        float right,
        float top,
        int color,
        float bottomLeft,
        float bottomRight,
        float topRight,
        float topLeft);
  }

  private PlanarProjection() {}

  public static boolean selected(int formation) {
    return formation >= FIRST_PRESET && formation < PRESET_COUNT;
  }

  public static SphereFormation style(int formation) {
    if (!selected(formation)) throw new IllegalArgumentException("Not a projection preset");
    return SphereFormation.values()[formation - FIRST_PRESET];
  }

  public static String label(int formation) {
    return switch (formation) {
      case 0 -> "Sweep";
      case 1 -> "Dissolve / reform";
      case 2 -> "Fade";
      case 4 -> "Scan line";
      case 6 -> "Tracing ribs";
      default -> selected(formation) ? style(formation).label() : "Sweep";
    };
  }

  public static float coverage(SphereFormation style, Frame f, float x, float y, float p) {
    if (p <= 0) return 0;
    if (p >= 1) return 1;
    float position =
        switch (style) {
          case LASER_CURTAIN -> f.travel(x, y);
          case RISING_RING -> f.horizontal ? f.travel(x, y) : f.y(y);
          case MERIDIAN_SWEEP -> .25f + .7f * f.travel(x, y);
          case PROJECTED_SEED -> Math.max(Math.abs(f.x(x) - .5f), Math.abs(f.y(y) - .5f)) * 2;
          case PLASMA_DISSOLVE -> .06f + .88f * (1 - PlasmaSurface.noise(x * .5, y * .5, 0, 0));
          case HEX_ASSEMBLY -> {
            float column = (float) Math.floor(x / .45f) * .45f;
            float row = (float) Math.floor(y / .5196f) * .5196f;
            double dx = f.x(column) - f.x(f.sourceX), dy = f.y(row) - f.y(f.sourceY);
            double farX = Math.max(f.x(f.sourceX), 1 - f.x(f.sourceX));
            double farY = Math.max(f.y(f.sourceY), 1 - f.y(f.sourceY));
            yield .04f + .9f * (float) (Math.hypot(dx, dy) / Math.hypot(farX, farY));
          }
        };
    return SphereFormation.smooth((style.front(p) - position + FRONT_WIDTH) / (2 * FRONT_WIDTH));
  }

  public static void fill(
      SphereFormation style,
      Frame frame,
      float left,
      float right,
      float bottom,
      float top,
      float progress,
      float opacity,
      int color,
      Patch patch) {
    if (progress <= 0 || opacity <= 0) return;
    if (progress >= 1) {
      patch.draw(
          left,
          bottom,
          right,
          top,
          color,
          .17f * opacity,
          .17f * opacity,
          .17f * opacity,
          .17f * opacity);
      return;
    }
    float cell = style == SphereFormation.PLASMA_DISSOLVE ? DISSOLVE_CELL_SIZE : CELL_SIZE;
    for (int x = (int) Math.floor(left / cell); x < Math.ceil(right / cell); x++)
      for (int y = (int) Math.floor(bottom / cell); y < Math.ceil(top / cell); y++) {
        float a = Math.max(left, x * cell), b = Math.min(right, (x + 1) * cell);
        float c = Math.max(bottom, y * cell), d = Math.min(top, (y + 1) * cell);
        patch.draw(
            a,
            c,
            b,
            d,
            color,
            alpha(style, frame, a, c, progress) * opacity,
            alpha(style, frame, b, c, progress) * opacity,
            alpha(style, frame, b, d, progress) * opacity,
            alpha(style, frame, a, d, progress) * opacity);
      }
  }

  private static float alpha(SphereFormation s, Frame f, float x, float y, float p) {
    float mask = coverage(s, f, x, y, p);
    return .17f * mask + .32f * 4 * mask * (1 - mask);
  }

  public static void guides(
      SphereFormation s, Frame f, float p, int color, HexFieldPattern.Stroke line) {
    if (p <= 0 || p >= 1) return;
    float front = (float) s.front(p), fade = Math.min(1, (1 - p) * 10);
    switch (s) {
      case LASER_CURTAIN -> {
        float x = f.targetX(front), y = f.targetY(front);
        for (int i = 0; i <= FAN_RAYS; i++) {
          float u = f.alongX() ? x : f.left + (f.right - f.left) * i / FAN_RAYS;
          float v = f.alongX() ? f.bottom + (f.top - f.bottom) * i / FAN_RAYS : y;
          line.draw(f.sourceX, f.sourceY, u, v, .012f, color, .28f * fade);
        }
        cross(f, front, color, fade, line);
      }
      case RISING_RING -> {
        if (f.horizontal) cross(f, front, color, fade, line);
        else
          glow(
              f.left,
              f.bottom + (f.top - f.bottom) * p,
              f.right,
              f.bottom + (f.top - f.bottom) * p,
              color,
              fade,
              line);
      }
      case MERIDIAN_SWEEP -> {
        for (int i = 0; i <= RIBS; i++) {
          float fraction = i / (float) RIBS, end = Math.min(1, p / .65f);
          if (f.alongX()) {
            float y = f.bottom + (f.top - f.bottom) * fraction;
            glow(f.sourceX, y, f.targetX(end), y, color, fade, line);
          } else {
            float x = f.left + (f.right - f.left) * fraction;
            glow(x, f.sourceY, x, f.targetY(end), color, fade, line);
          }
        }
      }
      case PROJECTED_SEED -> {
        float x = (f.left + f.right) / 2, y = (f.bottom + f.top) / 2;
        if (p < .27f)
          glow(
              f.sourceX,
              f.sourceY,
              x,
              y,
              color,
              Math.min(1, p / .08f) * Math.min(1, (.27f - p) / .08f),
              line);
        if (p > .13f) {
          float w = (f.right - f.left) * front / 2, h = (f.top - f.bottom) * front / 2;
          glow(x - w, y - h, x + w, y - h, color, fade, line);
          glow(x + w, y - h, x + w, y + h, color, fade, line);
          glow(x + w, y + h, x - w, y + h, color, fade, line);
          glow(x - w, y + h, x - w, y - h, color, fade, line);
        }
      }
      case HEX_ASSEMBLY, PLASMA_DISSOLVE -> {}
    }
  }

  private static void cross(
      Frame f, float progress, int color, float alpha, HexFieldPattern.Stroke line) {
    if (f.alongX())
      glow(f.targetX(progress), f.bottom, f.targetX(progress), f.top, color, alpha, line);
    else glow(f.left, f.targetY(progress), f.right, f.targetY(progress), color, alpha, line);
  }

  private static void glow(
      float x, float y, float X, float Y, int color, float alpha, HexFieldPattern.Stroke line) {
    line.draw(x, y, X, Y, .08f, color, .14f * alpha);
    line.draw(x, y, X, Y, .014f, 0xE0FFFF, .8f * alpha);
  }
}
