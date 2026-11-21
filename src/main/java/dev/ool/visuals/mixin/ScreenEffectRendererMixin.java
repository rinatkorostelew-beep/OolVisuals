package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    /** block texture drawn on the screen when your head is inside a block */
    @Inject(method = "renderTex", at = @At("HEAD"), cancellable = true, require = 0)
    private void ool$noBlockOverlay(CallbackInfo ci) {
        if (VisualsConfig.BLOCK_OVERLAY.value) {
            ci.cancel();
        }
    }
}
