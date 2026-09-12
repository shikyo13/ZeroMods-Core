package com.zeromods.core.animation;
import com.zeromods.core.tutorial.TutorialTimeline;
/** Shared interpolation for client animations; callers choose the clock, duration and geometry. */
public final class AnimationMath {
    private AnimationMath() {}
    public static double smoothstep(double time, double start, double end) { return TutorialTimeline.transition(time, start, end); }
    public static double lerp(double from, double to, double progress) { return TutorialTimeline.lerp(from, to, progress); }
    /** Radial pulse on a common world-space plane so adjacent surfaces share one wave. */
    public static double wave(double distance, double age, double speed, double width, double lifetime) {
        if (!Double.isFinite(distance) || !Double.isFinite(age) || !Double.isFinite(speed) || !Double.isFinite(width)
                || !Double.isFinite(lifetime) || distance < 0 || age < 0 || age >= lifetime || speed < 0 || width <= 0 || lifetime <= 0) return 0;
        double band = Math.max(0, 1 - Math.abs(distance - age * speed) / width);
        return band * (1 - age / lifetime);
    }
}
