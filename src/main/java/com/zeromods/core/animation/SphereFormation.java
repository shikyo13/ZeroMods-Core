package com.zeromods.core.animation;

/** Stateless shell reveal; height runs from the bottom of the shell to its crown. */
public enum SphereFormation {
  LASER_CURTAIN("Laser curtain"),
  RISING_RING("Rising scan ring"),
  HEX_ASSEMBLY("Hex assembly"),
  MERIDIAN_SWEEP("Meridian sweep"),
  PROJECTED_SEED("Projected seed"),
  PLASMA_DISSOLVE("Plasma dissolve");

  public static final double TAU = Math.PI * 2;
  private static final double FRONT_WIDTH = .035;
  private static final int HEX_ROWS = 24, HEX_COLUMNS = 64;
  public static final int MERIDIANS = 12;
  private final String label;

  SphereFormation(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }

  public SphereFormation next() {
    return values()[(ordinal() + 1) % values().length];
  }

  public static SphereFormation fromId(String id) {
    for (var style : values()) if (style.name().equals(id)) return style;
    return LASER_CURTAIN;
  }

  public double front(double progress) {
    return switch (this) {
      case LASER_CURTAIN -> Math.min(1, progress * progress * 1.15);
      case PROJECTED_SEED -> Math.max(0, (progress - .13) / .87);
      default -> progress;
    };
  }

  public float coverage(double azimuth, double height, double noise, float progress) {
    if (progress <= 0) return 0;
    if (progress >= 1) return 1;
    double position =
        switch (this) {
          case LASER_CURTAIN -> Math.acos(Math.cos(azimuth)) / Math.PI;
          case RISING_RING -> height;
          case HEX_ASSEMBLY -> {
            int column = Math.floorMod((int) Math.floor(azimuth / TAU * HEX_COLUMNS), HEX_COLUMNS);
            double row = Math.floor(height * HEX_ROWS) / HEX_ROWS;
            yield .04 + .90 * row + .035 * Math.sin(column * TAU / 8);
          }
          case MERIDIAN_SWEEP -> .28 + .68 * height;
          case PROJECTED_SEED -> 1 - height;
          case PLASMA_DISSOLVE -> .06 + .88 * (1 - noise);
        };
    return smooth((front(progress) - position + FRONT_WIDTH) / (FRONT_WIDTH * 2));
  }

  public float edge(double azimuth, double height, double noise, float progress) {
    if (progress <= 0 || progress >= 1) return 0;
    float mask = coverage(azimuth, height, noise, progress);
    return 4 * mask * (1 - mask);
  }

  public static float smooth(double x) {
    x = Math.max(0, Math.min(1, x));
    return (float) (x * x * (3 - 2 * x));
  }
}
