package com.zeromods.core.ui;
/** ARGB palette; consumers supply their own theme instead of modifying global constants. */
public record UiTheme(int background, int panel, int border, int text, int muted, int accent,
        int success, int warning, int error) {
    public static final UiTheme FLUX = new UiTheme(0xF01B1E21, 0xED101719, 0xFFBA8153,
            0xFFE4ECF2, 0xFF96A2B3, 0xFF52E4F5, 0xFF5EE68A, 0xFFFFD166, 0xFFFF6B6B);
    public UiTheme withAccent(int color) { return new UiTheme(background, panel, border, text, muted, color, success, warning, error); }
}
