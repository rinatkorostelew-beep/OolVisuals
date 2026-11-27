package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    /** arrows stuck in a body; only answered on the client thread, so a singleplayer server is untouched */
    @Inject(method = "getArrowCount", at = @At("HEAD"), cancellable = true, require = 0)
    private void ool$noArrows(CallbackInfoReturnable<Integer> cir) {
        if (VisualsConfig.ARROWS.value && Minecraft.getInstance().isSameThread()) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "getStingerCount", at = @At("HEAD"), cancellable = true, require = 0)
    private void ool$noStingers(CallbackInfoReturnable<Integer> cir) {
        if (VisualsConfig.ARROWS.value && Minecraft.getInstance().isSameThread()) {
            cir.setReturnValue(0);
        }
    }
}
