package com.zeromods.core.ui;
/** One transform for drawing and every pointer input, independent of Minecraft GUI scale. */
public record CanvasFit(double scale, int width, int height) {
    public static CanvasFit fit(int screenWidth, int screenHeight, int panelWidth, int panelHeight, int margin) {
        if (screenWidth <= 0 || screenHeight <= 0 || panelWidth <= 0 || panelHeight <= 0 || margin < 0)
            throw new IllegalArgumentException("Positive viewport and panel dimensions required");
        double scale = Math.min(1, Math.min(Math.max(1, screenWidth - margin * 2.0) / panelWidth,
                Math.max(1, screenHeight - margin * 2.0) / panelHeight));
        return new CanvasFit(scale, (int)(screenWidth / scale), (int)(screenHeight / scale));
    }
    public double pointer(double coordinate) { return coordinate / scale; }
}
