package dev.ool.visuals;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class VisualsScreen extends Screen {

    public VisualsScreen() {
        super(Component.literal("Visuals"));
    }

    private static Component label(VisualsConfig.Option o) {
        return Component.literal(o.label + ": " + (o.value ? "убрано" : "обычное"));
    }

    private static String onOff(boolean b) {
        return b ? "ВКЛ" : "выкл";
    }

    private static Component fastPlaceLabel() {
        return Component.literal("Быстрая установка: " + onOff(VisualsConfig.fastPlace));
    }

    private static Component delayLabel() {
        return Component.literal("Задержка: " + VisualsConfig.fastPlaceDelay + " тик. (обычно 4)");
    }

    private static Component hitLabel() {
        return Component.literal("Дистанция удара: " + onOff(VisualsConfig.hitDistance));
    }

    private static Component sprintLabel() {
        return Component.literal("Автоспринт: " + onOff(VisualsConfig.autoSprint));
    }

    private static Component aspectLabel() {
        return Component.literal("Aspect: " + onOff(VisualsConfig.aspectEnabled));
    }

    private static Component presetLabel() {
        return Component.literal("Aspect шаблон: "
                + String.format(Locale.ROOT, "%.2f", VisualsConfig.aspectRatio));
    }

    /** aspect ratio slider, ASPECT_MIN .. ASPECT_MAX */
    private static class AspectSlider extends AbstractSliderButton {
        AspectSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty(),
                    (VisualsConfig.aspectRatio - VisualsConfig.ASPECT_MIN)
                            / (VisualsConfig.ASPECT_MAX - VisualsConfig.ASPECT_MIN));
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Соотношение: "
                    + String.format(Locale.ROOT, "%.2f", VisualsConfig.aspectRatio)
                    + " (4:3 = 1.33, 16:9 = 1.78)"));
        }

        @Override
        protected void applyValue() {
            VisualsConfig.aspectRatio = (float) (VisualsConfig.ASPECT_MIN
                    + this.value * (VisualsConfig.ASPECT_MAX - VisualsConfig.ASPECT_MIN));
            VisualsConfig.save();
        }
    }

    @Override
    protected void init() {
        int h = 20;
        int colW = Math.min(160, (this.width - 30) / 2);
        int xLeft = this.width / 2 - colW - 3;
        int xRight = this.width / 2 + 3;
        int fullW = colW * 2 + 6;
        int y = Math.max(4, this.height / 2 - 112);

        int i = 0;
        for (VisualsConfig.Option o : VisualsConfig.OPTIONS) {
            int x = (i % 2 == 0) ? xLeft : xRight;
            Button b = Button.builder(label(o), btn -> {
                o.value = !o.value;
                btn.setMessage(label(o));
                VisualsConfig.save();
            }).bounds(x, y, colW, h).build();
            this.addRenderableWidget(b);
            if (i % 2 == 1) {
                y += 24;
            }
            i++;
        }
        if (i % 2 == 1) {
            y += 24;
        }

        // Fast Block Place
        this.addRenderableWidget(Button.builder(fastPlaceLabel(), btn -> {
            VisualsConfig.fastPlace = !VisualsConfig.fastPlace;
            btn.setMessage(fastPlaceLabel());
            VisualsConfig.save();
        }).bounds(xLeft, y, colW, h).build());

        this.addRenderableWidget(Button.builder(delayLabel(), btn -> {
            VisualsConfig.fastPlaceDelay = VisualsConfig.fastPlaceDelay % 3 + 1;   // 1 -> 2 -> 3 -> 1
            btn.setMessage(delayLabel());
            VisualsConfig.save();
        }).bounds(xRight, y, colW, h).build());
        y += 24;

        // Hit distance + Aspect on/off
        this.addRenderableWidget(Button.builder(hitLabel(), btn -> {
            VisualsConfig.hitDistance = !VisualsConfig.hitDistance;
            btn.setMessage(hitLabel());
            VisualsConfig.save();
        }).bounds(xLeft, y, colW, h).build());

        this.addRenderableWidget(Button.builder(aspectLabel(), btn -> {
            VisualsConfig.aspectEnabled = !VisualsConfig.aspectEnabled;
            btn.setMessage(aspectLabel());
            VisualsConfig.save();
        }).bounds(xRight, y, colW, h).build());
        y += 24;

        // Aspect presets (4:3 / 5:4 / ...) and slider
        this.addRenderableWidget(Button.builder(presetLabel(), btn -> {
            float cur = VisualsConfig.aspectRatio;
            float next = VisualsConfig.ASPECT_PRESETS[0];
            for (int k = 0; k < VisualsConfig.ASPECT_PRESETS.length; k++) {
                if (VisualsConfig.ASPECT_PRESETS[k] > cur + 0.005F) {
                    next = VisualsConfig.ASPECT_PRESETS[k];
                    break;
                }
            }
            VisualsConfig.aspectRatio = next;
            VisualsConfig.aspectEnabled = true;
            VisualsConfig.save();
            if (this.minecraft != null) {
                this.minecraft.setScreen(new VisualsScreen());   // rebuild so the slider moves too
            }
        }).bounds(xLeft, y, colW, h).build());

        this.addRenderableWidget(Button.builder(sprintLabel(), btn -> {
            VisualsConfig.autoSprint = !VisualsConfig.autoSprint;
            btn.setMessage(sprintLabel());
            VisualsConfig.save();
        }).bounds(xRight, y, colW, h).build());
        y += 24;

        this.addRenderableWidget(new AspectSlider(xLeft, y, fullW, h));
        y += 28;

        this.addRenderableWidget(
                Button.builder(Component.literal("Готово"), btn -> this.onClose())
                        .bounds(xLeft, y, fullW, h).build());
    }
}
