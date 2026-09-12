package com.zeromods.core.tutorial;
/** Context is a client drawing adapter, never a live world or an inventory mutation API.
 * Text can be Minecraft Component or another host's localized text type.
 */
public interface TutorialScene<C,T> {
    String id();
    T title();
    double durationSeconds();
    T caption(double seconds);
    void render(C context, double seconds);
    /** All caption variants, used to reserve space before playback starts. */
    default java.util.List<T> captions() { return java.util.List.of(caption(0)); }
}
