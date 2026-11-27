package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * FogRenderer#updateBuffer(ByteBuffer, int, Vector4f, float envStart, float envEnd,
 * float renderDistStart, float renderDistEnd, float skyEnd, float cloudEnd).
 * Pushing the fog start/end very far away removes the fog.
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    private static final float FAR = 1.0E7F;

    @ModifyVariable(method = "updateBuffer", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float ool$envStart(float value) {
        return VisualsConfig.FOG.value ? FAR : value;
    }

    @ModifyVariable(method = "updateBuffer", at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0)
    private float ool$envEnd(float value) {
        return VisualsConfig.FOG.value ? FAR + 256.0F : value;
    }

    @ModifyVariable(method = "updateBuffer", at = @At("HEAD"), argsOnly = true, ordinal = 2, require = 0)
    private float ool$renderDistStart(float value) {
        return VisualsConfig.FOG.value ? FAR : value;
    }

    @ModifyVariable(method = "updateBuffer", at = @At("HEAD"), argsOnly = true, ordinal = 3, require = 0)
    private float ool$renderDistEnd(float value) {
        return VisualsConfig.FOG.value ? FAR + 256.0F : value;
    }
}
