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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;


public class VisualsMod implements ClientModInitializer {

    private static boolean openScreen = false;
    private static boolean sprintForced = false;
    private static KeyMapping sprintToggleKey;

    public static String sprintKeyName() {
        return sprintToggleKey == null ? "G" : sprintToggleKey.getKey().getDisplayName().getString();
    }

    @Override
    public void onInitializeClient() {
        VisualsConfig.load();

        // Auto sprint toggle key: unbound by default, set it in Controls -> Movement
        sprintToggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.oolvisuals.sprint", GLFW.GLFW_KEY_G, KeyMapping.Category.MOVEMENT));

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
            applySoftAim(client);
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

        // distance of your last hit on a player, shown in the top-right corner
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
            String text = HitDistance.displayText();
            int x = mc.getWindow().getGuiScaledWidth() - mc.font.width(text) - 6;   // top-right corner
            graphics.drawString(mc.font, text, x, 6, color, true);
        });
    }
    /**
     * Practice-only soft aim: while the attack key is held, gently rotates toward
     * the best visible player inside a narrow cone and configured range.
     * No packets, auto-attacks, target snapping, or anti-cheat bypass are used.
     */
    private static void applySoftAim(Minecraft client) {
        if (!VisualsConfig.softAim || client.player == null || client.level == null
                || client.screen != null || !client.options.keyAttack.isDown()) {
            return;
        }

        Player self = client.player;
        Vec3 eye = self.getEyePosition();
        float bestAngle = 22.0F;
        float bestYaw = 0.0F;
        float bestPitch = 0.0F;
        boolean foundTarget = false;
        double maxDistanceSq = VisualsConfig.softAimRange * VisualsConfig.softAimRange;
        float currentYaw = self.getYRot();
        float currentPitch = self.getXRot();

        for (Player candidate : client.level.players()) {
            if (candidate == self || !candidate.isAlive() || candidate.isSpectator()) {
                continue;
            }

            // Cheap distance/cone checks run before the more expensive visibility ray check.
            Vec3 delta = candidate.getBoundingBox().getCenter().subtract(eye);
            double distanceSq = delta.lengthSqr();
            if (distanceSq > maxDistanceSq || distanceSq < 0.01D) {
                continue;
            }

            float desiredYaw = (float) (Math.atan2(delta.z, delta.x) * (180.0D / Math.PI)) - 90.0F;
            float desiredPitch = (float) -(Math.atan2(delta.y,
                    Math.sqrt(delta.x * delta.x + delta.z * delta.z)) * (180.0D / Math.PI));
            float yawError = Mth.wrapDegrees(desiredYaw - currentYaw);
            float pitchError = Mth.wrapDegrees(desiredPitch - currentPitch);
            float angle = (float) Math.sqrt(yawError * yawError + pitchError * pitchError);
            if (angle >= bestAngle || !self.hasLineOfSight(candidate)) {
                continue;
            }

            bestAngle = angle;
            bestYaw = desiredYaw;
            bestPitch = desiredPitch;
            foundTarget = true;
        }

        if (!foundTarget) {
            return;
        }
        float strength = VisualsConfig.softAimStrength;
        self.setYRot(currentYaw + Mth.wrapDegrees(bestYaw - currentYaw) * strength);
        self.setXRot(currentPitch + Mth.wrapDegrees(bestPitch - currentPitch) * strength);
    }

}
