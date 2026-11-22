package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * "Stretched resolution": the projection matrix that is uploaded to the GPU gets its X scale
 * multiplied, so everything looks wider (like 4:3 on a 16:9 screen).
 * Culling uses a different matrix, so only widening (>= 1.0) is allowed.
 */
@Mixin(PerspectiveProjectionMatrixBuffer.class)
public class PerspectiveProjectionMatrixBufferMixin {

    @ModifyVariable(method = "getBuffer", at = @At("HEAD"), argsOnly = true, require = 0)
    private Matrix4f ool$stretch(Matrix4f matrix) {
        float s = VisualsConfig.stretch;
        if (s <= 1.0001F) {
            return matrix;
        }
        Matrix4f copy = new Matrix4f(matrix);
        copy.m00(copy.m00() * s);
        return copy;
    }
}
