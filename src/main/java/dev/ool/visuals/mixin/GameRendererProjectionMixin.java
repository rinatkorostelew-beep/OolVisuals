package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererProjectionMixin {

    /** Aspect, first hook: GameRenderer#getProjectionMatrix(float fov) */
    @Inject(method = "getProjectionMatrix", at = @At("RETURN"), cancellable = true, require = 0)
    private void ool$aspectProjection(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        Matrix4f original = cir.getReturnValue();
        Matrix4f scaled = VisualsConfig.applyAspect(original);
        if (scaled != original) {
            cir.setReturnValue(scaled);
        }
    }
}
