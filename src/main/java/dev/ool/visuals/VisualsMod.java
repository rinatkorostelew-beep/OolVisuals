package dev.ool.visuals;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class VisualsMod implements ClientModInitializer {

    private static boolean openScreen = false;
    private static boolean sprintForced = false;
    private static KeyMapping sprintToggleKey;

    @Override
    public void onInitializeClient() {
        VisualsConfig.load();

        // Auto sprint toggle key: unbound by default, set it in Controls -> Movement
        sprintToggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.oolvisuals.sprint", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.Category.MOVEMENT));

        // /visuals opens the settings screen (a tick later, so the chat screen doesn't close it)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("visuals").executes(ctx -> {
                    openScreen = true;
                    return Command.SINGLE_SUCCESS;
                })));

        // auto sprint: while W is held, keep the vanilla Sprint key "pressed" (the game applies hunger etc. itself)
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null || client.screen != null) {
                return;
            }
            if (VisualsConfig.autoSprint && client.options.keyUp.isDown()) {
                client.options.keySprint.setDown(true);
                sprintForced = true;
            } else if (sprintForced) {
                client.options.keySprint.setDown(false);
                sprintForced = false;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openScreen && client.screen == null) {
                openScreen = false;
                client.setScreen(new VisualsScreen());
            }
            while (sprintToggleKey.consumeClick()) {
                VisualsConfig.autoSprint = !VisualsConfig.autoSprint;
                VisualsConfig.save();
                if (client.player != null) {
                    client.player.displayClientMessage(Component.literal(
                            "Автоспринт: " + (VisualsConfig.autoSprint ? "ВКЛ" : "выкл")), true);
                }
            }
        });

        // distance of your last hit on a player, shown under the crosshair
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            if (!VisualsConfig.hitDistance) {
                return;
            }
            double d = HitDistance.current();
            if (d < 0) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) {
                return;
            }
            int color = d >= 2.5 ? 0xFF55FF55 : (d >= 1.5 ? 0xFFFFFF55 : 0xFFFF5555);
            String text = "Удар с " + String.format(Locale.ROOT, "%.2f", d) + " бл.";
            graphics.drawCenteredString(mc.font, text, mc.getWindow().getGuiScaledWidth() / 2,
                    mc.getWindow().getGuiScaledHeight() / 2 + 14, color);
        });
    }
}
