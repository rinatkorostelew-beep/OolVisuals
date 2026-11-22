package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * The game skips everything outside the culling frustum (banners, chests, signs, entities...).
 * When Aspect changes the picture, the frustum must be AT LEAST as wide as what you see,
 * otherwise things near the edges of the screen disappear. We keep it a bit wider than needed.
 */
@Mixin(Frustum.class)
public class FrustumMixin {

    /** Frustum#calculateFrustum(Matrix4f positionMatrix, Matrix4f projectionMatrix): ordinal 1 = projection */
    @ModifyVariable(method = "calculateFrustum", at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0)
    private Matrix4f ool$widenCulling(Matrix4f projection) {
        float s = VisualsConfig.aspectScale();
        if (Math.abs(s - 1.0F) < 0.0001F) {
            return projection;
        }
        Matrix4f copy = new Matrix4f(projection);
        copy.m00(copy.m00() * Math.min(s, 1.0F) * 0.85F);
        return copy;
    }
}
