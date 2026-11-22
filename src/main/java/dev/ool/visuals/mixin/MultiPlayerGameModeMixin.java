package dev.ool.visuals.mixin;

import dev.ool.visuals.HitDistance;
import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    /** you attack an entity: if it is a player, remember the distance */
    @Inject(method = "attack", at = @At("HEAD"), require = 0)
    private void ool$recordHit(Player player, Entity target, CallbackInfo ci) {
        if (VisualsConfig.hitDistance && target instanceof Player && player == Minecraft.getInstance().player) {
            HitDistance.record(player, target);
        }
    }
}
