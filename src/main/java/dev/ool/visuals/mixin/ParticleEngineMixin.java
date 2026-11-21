package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    /** particles when a block is broken */
    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true, require = 0)
    private void ool$noDestroy(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (VisualsConfig.BREAK_PARTICLES.value) {
            ci.cancel();
        }
    }

    /** particles while a block is being mined */
    @Inject(method = "crack", at = @At("HEAD"), cancellable = true, require = 0)
    private void ool$noCrack(BlockPos pos, Direction side, CallbackInfo ci) {
        if (VisualsConfig.BREAK_PARTICLES.value) {
            ci.cancel();
        }
    }

    /** every particle (off by default, it also hides crit/hit effects) */
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true, require = 0)
    private void ool$noParticles(ParticleOptions options, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed,
                                 CallbackInfoReturnable<Particle> cir) {
        if (VisualsConfig.ALL_PARTICLES.value) {
            cir.setReturnValue(null);
        }
    }
}
