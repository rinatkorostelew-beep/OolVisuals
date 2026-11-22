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

    private static Component fastPlaceLabel() {
        return Component.literal("Быстрая установка блоков: " + (VisualsConfig.fastPlace ? "ВКЛ" : "выкл"));
    }

    private static Component delayLabel() {
        return Component.literal("Задержка установки: " + VisualsConfig.fastPlaceDelay + " тик. (обычно 4)");
    }

    /** horizontal stretch slider, 1.00x .. 1.80x */
    private static class StretchSlider extends AbstractSliderButton {
        StretchSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty(),
                    (VisualsConfig.stretch - VisualsConfig.STRETCH_MIN)
                            / (VisualsConfig.STRETCH_MAX - VisualsConfig.STRETCH_MIN));
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(
                    "Растяжка экрана: " + String.format(Locale.ROOT, "%.2f", VisualsConfig.stretch) + "x"));
        }

        @Override
        protected void applyValue() {
            VisualsConfig.stretch = (float) (VisualsConfig.STRETCH_MIN
                    + this.value * (VisualsConfig.STRETCH_MAX - VisualsConfig.STRETCH_MIN));
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
        int y = Math.max(6, this.height / 2 - 100);

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

        this.addRenderableWidget(new StretchSlider(xLeft, y, fullW, h));
        y += 28;

        this.addRenderableWidget(
                Button.builder(Component.literal("Готово"), btn -> this.onClose())
                        .bounds(xLeft, y, fullW, h).build());
    }
}
