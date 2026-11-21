package dev.ool.visuals.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /** hurt camera shake */
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true, require = 0)
    private void ool$noShake(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        if (VisualsConfig.SHAKE.value) {
            ci.cancel();
        }
    }

    /** totem pop animation on screen */
    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true, require = 0)
    private void ool$noTotem(ItemStack stack, CallbackInfo ci) {
        if (VisualsConfig.TOTEM.value) {
            ci.cancel();
        }
    }
}
