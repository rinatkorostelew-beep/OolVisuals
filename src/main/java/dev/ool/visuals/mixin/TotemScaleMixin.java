package dev.ool.visuals.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * In 1.21.11 the totem pop is drawn by ScreenEffectRenderer#renderItemActivationAnimation(PoseStack, float, ...).
 * Scaling that PoseStack right at the start makes the whole animation (item and its movement) smaller.
 */
@Mixin(ScreenEffectRenderer.class)
public class TotemScaleMixin {

    @ModifyVariable(method = "renderItemActivationAnimation", at = @At("HEAD"), argsOnly = true, require = 0)
    private PoseStack ool$smallTotem(PoseStack poseStack) {
        float s = VisualsConfig.totemScale;
        if (s > 0.001F && s < 0.999F) {
            poseStack.scale(s, s, s);
        }
        return poseStack;
    }
}
