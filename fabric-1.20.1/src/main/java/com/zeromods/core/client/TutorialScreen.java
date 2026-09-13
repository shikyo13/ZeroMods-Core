package com.zeromods.core.client;
import com.zeromods.core.tutorial.*;
import com.zeromods.core.ui.UiTheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Client-only demonstrations. Opening a lesson never changes a player's network or inventory. */
public final class TutorialScreen extends FittedScreen {
    private final int ACCENT, COPPER, TEXT, MUTED;
    private final UiTheme theme;
    private final TutorialLesson<GuiGraphics, Component> lesson;
    private final Component help;
    private final Screen parent;
    private final TutorialPlaybackController playback;
    private final List<Button> chapters = new ArrayList<>();
    private TutorialSeekBar timeline;
    private Transport previous, play, next;
    private boolean renderingWidgets;
    private int chapterPage;
    private int left, top, panelWidth, panelHeight, sceneTop, sceneHeight, captionTop, controlsTop;

    public TutorialScreen(Screen parent, Component title, Component help,
            TutorialLesson<GuiGraphics, Component> lesson, UiTheme theme, int chapter) {
        super(title);
        this.parent = parent; this.lesson = lesson; this.theme = theme; this.help = help;
        ACCENT = theme.accent(); COPPER = theme.border(); TEXT = theme.text(); MUTED = theme.muted();
        this.playback = lesson.playback(chapter);
    }

    static Component text(String key, Object... values) {
        return Component.translatable("screen.zeromodscore.guide." + key, values);
    }

    @Override protected void init() {
        fit(540, 344);
        chapters.clear();
        chapterPage = playback.sceneIndex() / 5;
        panelWidth = Math.min(540, width - 12);
        panelHeight = Math.min(344, height - 8);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int noteHeight = font.split(help, panelWidth - 24).size() * font.lineHeight;
        int chapterTop = top + 23 + noteHeight;
        int chapterWidth = (panelWidth - 24 - 2 * (Math.min(5, lesson.scenes().size()) - 1)) / Math.min(5, lesson.scenes().size());
        for (int index = chapterPage * 5; index < Math.min(lesson.scenes().size(), (chapterPage + 1) * 5); index++) {
            int chapter = index;
            Button button = Button.builder(lesson.scenes().get(index).title(), ignored -> playback.setScene(chapter))
                    .bounds(left + 12 + (index % 5) * (chapterWidth + 2), chapterTop, chapterWidth, 20).build();
            button.setTooltip(Tooltip.create(lesson.scenes().get(index).title()));
            chapters.add(addRenderableWidget(button));
        }
        sceneTop = chapterTop + 25;
        controlsTop = top + panelHeight - 28;
        int captionHeight = 0;
        for (var scene : lesson.scenes()) {
            for (var caption : scene.captions()) {
                captionHeight = Math.max(captionHeight, font.split(caption, panelWidth - 24).size() * font.lineHeight);
            }
        }
        captionTop = controlsTop - 16 - 6 - captionHeight;
        sceneHeight = Math.max(16, captionTop - sceneTop - 7);
        timeline = addRenderableWidget(new TutorialSeekBar(left + 12, controlsTop - 16, panelWidth - 24, playback, ACCENT));
        previous = transport(0, 0, "previous", ignored -> playback.previous());
        transport(1, 1, "replay", ignored -> playback.replay());
        play = transport(2, 2, "pause", ignored -> playback.togglePaused());
        next = transport(3, 3, "next", ignored -> playback.next());
        addRenderableWidget(Button.builder(text("done"), ignored -> onClose())
                .bounds(left + panelWidth - 76, controlsTop, 64, 22).build());
    }

    private Transport transport(int position, int icon, String label, Button.OnPress action) {
        return addRenderableWidget(new Transport(left + 12 + position * 24, controlsTop, icon, text(label), action));
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (chapterPage != playback.sceneIndex() / 5) rebuildWidgets();
        mouseX = fitMouse(mouseX); mouseY = fitMouse(mouseY);
        beginFit(graphics);
        try {
        playback.onFrame(System.nanoTime());
        renderBackground(graphics);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, theme.background());
        graphics.renderOutline(left, top, panelWidth, panelHeight, COPPER);
        graphics.drawCenteredString(font, title, width / 2, top + 8, ACCENT);
        int y = top + 20;
        for (var line : font.split(help, panelWidth - 24)) {
            graphics.drawString(font, line, width / 2 - font.width(line) / 2, y, MUTED, false);
            y += font.lineHeight;
        }
        int chapter = playback.sceneIndex();
        for (int index = 0; index < chapters.size(); index++) chapters.get(index).active = chapterPage * 5 + index != chapter;
        previous.active = chapter > 0 || playback.elapsedSeconds() > 1;
        next.active = chapter < lesson.scenes().size() - 1;
        play.setPlaying(!playback.paused());

        graphics.fill(left + 12, sceneTop, left + panelWidth - 12, sceneTop + sceneHeight, theme.panel());
        graphics.renderOutline(left + 12, sceneTop, panelWidth - 24, sceneHeight, 0xFF3C5157);
        fitScissor(graphics, left + 13, sceneTop + 1, left + panelWidth - 13, sceneTop + sceneHeight - 1);
        float scale = Math.min((panelWidth - 26) / 480f, (sceneHeight - 2) / 180f);
        graphics.pose().pushPose();
        graphics.pose().translate(width / 2f - 240 * scale, sceneTop + (sceneHeight - 180 * scale) / 2f, 0);
        graphics.pose().scale(scale, scale, 1);
        lesson.scenes().get(chapter).render(graphics, playback.elapsedSeconds());
        graphics.pose().popPose();
        graphics.disableScissor();
        int captionY = captionTop;
        for (var line : font.split(lesson.scenes().get(chapter).caption(playback.elapsedSeconds()), panelWidth - 24)) {
            graphics.drawString(font, line, left + 12, captionY, TEXT, false);
            captionY += font.lineHeight;
        }
        if (panelWidth >= 290) graphics.drawCenteredString(font, text("time", (int) playback.elapsedSeconds(),
                (int) playback.duration()), (left + 112 + left + panelWidth - 82) / 2, controlsTop + 7, MUTED);
        renderingWidgets = true;
        try { super.render(graphics, mouseX, mouseY, delta); }
        finally { renderingWidgets = false; }
        } finally { graphics.pose().popPose(); }
    }

    @Override public void renderBackground(GuiGraphics graphics) {
        if (!renderingWidgets) super.renderBackground(graphics);
    }

    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (getFocused() == timeline && (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT
                || key == GLFW.GLFW_KEY_HOME || key == GLFW.GLFW_KEY_END)) return timeline.keyPressed(key, scanCode, modifiers);
        if (key == GLFW.GLFW_KEY_SPACE) playback.togglePaused();
        else if (key == GLFW.GLFW_KEY_R) playback.replay();
        else if (key == GLFW.GLFW_KEY_LEFT) playback.previous();
        else if (key == GLFW.GLFW_KEY_RIGHT) playback.next();
        else return super.keyPressed(key, scanCode, modifiers);
        return true;
    }

    @Override public void onClose() { Minecraft.getInstance().setScreen(parent); }
    @Override public boolean isPauseScreen() { return true; }

    private final class Transport extends Button {
        private int icon;
        private boolean playing = true;

        Transport(int x, int y, int icon, Component label, OnPress action) {
            super(x, y, 22, 22, label, action, DEFAULT_NARRATION);
            this.icon = icon;
            setTooltip(Tooltip.create(label));
        }

        void setPlaying(boolean value) {
            if (playing == value) return;
            playing = value;
            icon = value ? 2 : 4;
            setMessage(text(value ? "pause" : "play"));
            setTooltip(Tooltip.create(getMessage()));
        }

        @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int color = active ? (isHoveredOrFocused() ? ACCENT : TEXT) : 0xFF566268;
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF283338);
            graphics.renderOutline(getX(), getY(), width, height, isHoveredOrFocused() ? COPPER : 0xFF4D585B);
            int x = getX() + 7, y = getY() + 6;
            if (icon == 2) {
                graphics.fill(x, y, x + 2, y + 10, color);
                graphics.fill(x + 5, y, x + 7, y + 10, color);
            } else if (icon == 1) {
                graphics.fill(x, y, x + 8, y + 2, color);
                graphics.fill(x + 6, y + 2, x + 8, y + 8, color);
                graphics.fill(x, y + 8, x + 8, y + 10, color);
                graphics.fill(x - 2, y + 4, x, y + 8, color);
                for (int row = 0; row < 5; row++) {
                    int length = 3 - Math.abs(row - 2);
                    graphics.fill(x - length, y - 1 + row, x + 1, y + row, color);
                }
            } else {
                boolean backwards = icon == 0;
                for (int row = 0; row < 9; row++) {
                    int length = 5 - Math.abs(row - 4);
                    int start = backwards ? x + 6 - length : x;
                    graphics.fill(start, y + row, start + length, y + row + 1, color);
                }
                if (icon == 0) graphics.fill(x - 2, y, x - 1, y + 9, color);
                if (icon == 3) graphics.fill(x + 7, y, x + 8, y + 9, color);
            }
        }
    }
}
