package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Aspect, second hook: the matrix that is uploaded to the GPU (skipped if it was already scaled). */
@Mixin(PerspectiveProjectionMatrixBuffer.class)
public class PerspectiveProjectionMatrixBufferMixin {

    @ModifyVariable(method = "getBuffer", at = @At("HEAD"), argsOnly = true, require = 0)
    private Matrix4f ool$aspect(Matrix4f matrix) {
        return VisualsConfig.applyAspect(matrix);
    }
}
