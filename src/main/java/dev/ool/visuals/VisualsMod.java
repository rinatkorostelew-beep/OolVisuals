package dev.ool.visuals;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class VisualsMod implements ClientModInitializer {

    private static boolean openScreen = false;

    @Override
    public void onInitializeClient() {
        VisualsConfig.load();

        // /visuals opens the settings screen (a tick later, so the chat screen doesn't close it)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("visuals").executes(ctx -> {
                    openScreen = true;
                    return Command.SINGLE_SUCCESS;
                })));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openScreen && client.screen == null) {
                openScreen = false;
                client.setScreen(new VisualsScreen());
            }
        });
    }
}
