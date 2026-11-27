package dev.ool.visuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Settings menu with its own dark "client" look. The widgets are vanilla Buttons (so clicks and keys keep working),
 * but they draw their own contents over the whole button area.
 */
public class VisualsScreen extends Screen {

    private static final int BG = 0xF008090D;
    private static final int ROW = 0xFF17131A;
    private static final int ROW_HOVER = 0xFF29202B;
    private static final int ACCENT = 0xFFFF3048;
    private static final int TEXT = 0xFFF7F2F4;
    private static final int MUTED = 0xFFB49AA2;
    private static final int OFF = 0xFF49343B;
    private static final int PANEL_EDGE = 0xFF742033;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private final List<String> headerTexts = new ArrayList<>();
    private final List<Integer> headerYs = new ArrayList<>();

    public VisualsScreen() {
        super(Component.literal("Visuals"));
    }

    private static String onOff(boolean b) {
        return b ? "ВКЛ" : "выкл";
    }

    private static String ratioText() {
        return String.format(Locale.ROOT, "%.2f", VisualsConfig.aspectRatio);
    }

    private static String totemText() {
        return VisualsConfig.totemScale <= 0.001F ? "убран" : Math.round(VisualsConfig.totemScale * 100.0F) + "%";
    }

    private static void setRatio(float r) {
        r = Math.round(r * 100.0F) / 100.0F;
        VisualsConfig.aspectRatio = Math.max(VisualsConfig.ASPECT_MIN, Math.min(VisualsConfig.ASPECT_MAX, r));
        VisualsConfig.aspectEnabled = true;
        VisualsConfig.save();
    }

    /** a row: label on the left, and either an on/off switch or a value on the right */
    private static class Row extends Button {
        private final Supplier<String> label;
        private final Supplier<String> value;      // may be null
        private final BooleanSupplier toggle;      // may be null
        private final boolean centered;
        private final boolean accent;

        Row(int x, int y, int w, int h, Supplier<String> label, Supplier<String> value,
            BooleanSupplier toggle, boolean centered, boolean accent, Button.OnPress press) {
            super(x, y, w, h, Component.empty(), press, Button.DEFAULT_NARRATION);
            this.label = label;
            this.value = value;
            this.toggle = toggle;
            this.centered = centered;
            this.accent = accent;
        }

        @Override
        protected void renderContents(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();
            boolean hover = this.isHoveredOrFocused();

            int bg = accent ? (hover ? 0xFF6A94FF : ACCENT) : (hover ? ROW_HOVER : ROW);
            g.fill(x, y, x + w, y + h, bg);          // covers the vanilla button sprite completely

            int textY = y + (h - 8) / 2;
            String text = label.get();
            if (centered) {
                g.drawString(font, text, x + (w - font.width(text)) / 2, textY, TEXT, false);
                return;
            }
            g.drawString(font, text, x + 6, textY, TEXT, false);

            if (toggle != null) {
                boolean on = toggle.getAsBoolean();
                int sw = 22;
                int sh = Math.max(8, h - 8);
                int sx = x + w - sw - 6;
                int sy = y + (h - sh) / 2;
                g.fill(sx, sy, sx + sw, sy + sh, on ? ACCENT : OFF);
                int k = sh - 2;
                int kx = on ? sx + sw - k - 1 : sx + 1;
                g.fill(kx, sy + 1, kx + k, sy + 1 + k, 0xFFFFFFFF);
            }
            if (value != null) {
                String v = value.get();
                int right = x + w - (toggle != null ? 34 : 6);
                g.drawString(font, v, right - font.width(v), textY, ACCENT, false);
            }
        }
    }

    private void header(String text, int y) {
        headerTexts.add(text);
        headerYs.add(y);
    }

    @Override
    protected void init() {
        headerTexts.clear();
        headerYs.clear();

        int pad = 8;
        int gap = 4;
        int headerH = 11;
        int titleH = 18;
        int totalRows = 12;

        panelW = Math.min(this.width - 8, 400);
        panelX = (this.width - panelW) / 2;
        int colW = (panelW - pad * 2 - gap) / 2;
        int xL = panelX + pad;
        int xR = xL + colW + gap;
        int fullW = colW * 2 + gap;

        int fixed = titleH + 4 * headerH + pad * 2;
        int rowH = Math.max(12, Math.min(18, (this.height - 8 - fixed - totalRows * 2) / totalRows));
        int step = rowH + 2;
        panelH = fixed + totalRows * step;
        panelY = Math.max(4, (this.height - panelH) / 2);

        int y = panelY + titleH + pad / 2;

        // ---- effects removed ----
        header("УБРАТЬ ЭФФЕКТЫ (ВКЛ = убрано)", y);
        y += headerH;
        int i = 0;
        for (VisualsConfig.Option o : VisualsConfig.OPTIONS) {
            int x = (i % 2 == 0) ? xL : xR;
            this.addRenderableWidget(new Row(x, y, colW, rowH, () -> o.label, null, () -> o.value, false, false, b -> {
                o.value = !o.value;
                VisualsConfig.save();
            }));
            if (i % 2 == 1) {
                y += step;
            }
            i++;
        }
        // totem animation size (click to cycle: hidden / 25% / 50% / 75% / 100%)
        int totemX = (i % 2 == 0) ? xL : xR;
        this.addRenderableWidget(new Row(totemX, y, colW, rowH, () -> "Размер тотема",
                VisualsScreen::totemText, null, false, false, b -> {
            float cur = VisualsConfig.totemScale;
            float next = VisualsConfig.TOTEM_PRESETS[0];
            for (float p : VisualsConfig.TOTEM_PRESETS) {
                if (p > cur + 0.01F) {
                    next = p;
                    break;
                }
            }
            VisualsConfig.totemScale = next;
            VisualsConfig.save();
        }));
        if (i % 2 == 1) {
            y += step;
        }
        i++;
        if (i % 2 == 1) {
            y += step;
        }

        // ---- gameplay ----
        header("ГЕЙМПЛЕЙ", y);
        y += headerH;
        this.addRenderableWidget(new Row(xL, y, colW, rowH, () -> "Быстрая установка", null,
                () -> VisualsConfig.fastPlace, false, false, b -> {
            VisualsConfig.fastPlace = !VisualsConfig.fastPlace;
            VisualsConfig.save();
        }));
        this.addRenderableWidget(new Row(xR, y, colW, rowH, () -> "Задержка (обычно 4)",
                () -> VisualsConfig.fastPlaceDelay + " тик.", null, false, false, b -> {
            VisualsConfig.fastPlaceDelay = VisualsConfig.fastPlaceDelay % 3 + 1;   // 1 -> 2 -> 3 -> 1
            VisualsConfig.save();
        }));
        y += step;
        this.addRenderableWidget(new Row(xL, y, colW, rowH, () -> "Дистанция удара", null,
                () -> VisualsConfig.hitDistance, false, false, b -> {
            VisualsConfig.hitDistance = !VisualsConfig.hitDistance;
            VisualsConfig.save();
        }));
        this.addRenderableWidget(new Row(xR, y, colW, rowH, () -> "Автоспринт",
                () -> "БИНД: " + VisualsMod.sprintKeyName(),
                () -> VisualsConfig.autoSprint, false, false, b -> {
            VisualsConfig.autoSprint = !VisualsConfig.autoSprint;
            VisualsConfig.save();
        }));
        y += step;

        // ---- soft aim (private-server practice) ----
        header("SOFT AIM (ТРЕНИРОВКА / ПРИВАТНЫЙ СЕРВЕР)", y);
        y += headerH;
        this.addRenderableWidget(new Row(xL, y, colW, rowH, () -> "Плавная доводка", null,
                () -> VisualsConfig.softAim, false, false, b -> {
            VisualsConfig.softAim = !VisualsConfig.softAim;
            VisualsConfig.save();
        }));
        this.addRenderableWidget(new Row(xR, y, colW, rowH, () -> "Сила доводки",
                () -> Math.round(VisualsConfig.softAimStrength * 100) + "%", null, false, false, b -> {
            VisualsConfig.softAimStrength += 0.05F;
            if (VisualsConfig.softAimStrength > 0.3001F) VisualsConfig.softAimStrength = 0.05F;
            VisualsConfig.save();
        }));
        y += step;
        this.addRenderableWidget(new Row(xL, y, colW, rowH, () -> "Дистанция захвата",
                () -> String.format(Locale.ROOT, "%.0f бл.", VisualsConfig.softAimRange), null, false, false, b -> {
            VisualsConfig.softAimRange += 1.0F;
            if (VisualsConfig.softAimRange > 8.0F) VisualsConfig.softAimRange = 3.0F;
            VisualsConfig.save();
        }));
        this.addRenderableWidget(new Row(xR, y, colW, rowH, () -> "Условие", () -> "ЛКМ зажата", null,
                false, false, b -> { }));
        y += step;

        // ---- aspect ----
        header("ЭКРАН (ASPECT): 4:3 = 1.33, 16:9 = 1.78", y);
        y += headerH;
        this.addRenderableWidget(new Row(xL, y, colW, rowH, () -> "Aspect", null,
                () -> VisualsConfig.aspectEnabled, false, false, b -> {
            VisualsConfig.aspectEnabled = !VisualsConfig.aspectEnabled;
            VisualsConfig.save();
        }));
        this.addRenderableWidget(new Row(xR, y, colW, rowH, () -> "Шаблон (нажми)",
                VisualsScreen::ratioText, null, false, false, b -> {
            float cur = VisualsConfig.aspectRatio;
            float next = VisualsConfig.ASPECT_PRESETS[0];
            for (float p : VisualsConfig.ASPECT_PRESETS) {
                if (p > cur + 0.005F) {
                    next = p;
                    break;
                }
            }
            setRatio(next);
        }));
        y += step;

        int small = 24;
        this.addRenderableWidget(new Row(xL, y, small, rowH, () -> "-", null, null, true, false,
                b -> setRatio(VisualsConfig.aspectRatio - 0.05F)));
        this.addRenderableWidget(new Row(xL + small + gap, y, fullW - 2 * small - 2 * gap, rowH,
                () -> "Соотношение", VisualsScreen::ratioText, null, false, false, b -> { }));
        this.addRenderableWidget(new Row(xL + fullW - small, y, small, rowH, () -> "+", null, null, true, false,
                b -> setRatio(VisualsConfig.aspectRatio + 0.05F)));
        y += step;

        // ---- done ----
        this.addRenderableWidget(new Row(xL, y, fullW, rowH, () -> "Готово", null, null, true, true,
                b -> this.onClose()));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        g.fill(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, PANEL_EDGE);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG);
        g.fill(panelX, panelY, panelX + panelW, panelY + 3, ACCENT);
        g.fill(panelX, panelY + 19, panelX + panelW, panelY + 20, 0xFF34202A);
        g.drawString(this.font, "OOL VISUALS", panelX + 10, panelY + 7, TEXT, true);
        String subtitle = "CLIENT SETTINGS  /  1.21.11";
        g.drawString(this.font, subtitle, panelX + panelW - this.font.width(subtitle) - 10,
                panelY + 8, MUTED, false);
        for (int k = 0; k < headerTexts.size(); k++) {
            g.drawString(this.font, headerTexts.get(k), panelX + 8, headerYs.get(k) + 1, MUTED, false);
        }
    }
}
