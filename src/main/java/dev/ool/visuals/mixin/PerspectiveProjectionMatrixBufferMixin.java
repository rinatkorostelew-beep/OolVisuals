package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Aspect ratio of the 3D picture: scales the X axis of the projection matrix that goes to the GPU. */
@Mixin(PerspectiveProjectionMatrixBuffer.class)
public class PerspectiveProjectionMatrixBufferMixin {

    @ModifyVariable(method = "getBuffer", at = @At("HEAD"), argsOnly = true, require = 0)
    private Matrix4f ool$aspect(Matrix4f matrix) {
        float s = VisualsConfig.aspectScale();
        if (Math.abs(s - 1.0F) < 0.0001F) {
            return matrix;
        }
        Matrix4f copy = new Matrix4f(matrix);
        copy.m00(copy.m00() * s);
        return copy;
    }
}
