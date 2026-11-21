package dev.ool.visuals;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VisualsScreen extends Screen {

    public VisualsScreen() {
        super(Component.literal("Visuals"));
    }

    private static Component label(VisualsConfig.Option o) {
        return Component.literal(o.label + ": " + (o.value ? "убрано" : "обычное"));
    }

    @Override
    protected void init() {
        int w = 230;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 95;

        for (VisualsConfig.Option o : VisualsConfig.OPTIONS) {
            Button b = Button.builder(label(o), btn -> {
                o.value = !o.value;
                btn.setMessage(label(o));
                VisualsConfig.save();
            }).bounds(x, y, w, h).build();
            this.addRenderableWidget(b);
            y += 24;
        }

        this.addRenderableWidget(
                Button.builder(Component.literal("Готово"), btn -> this.onClose())
                        .bounds(x, y + 8, w, h).build());
    }
}
