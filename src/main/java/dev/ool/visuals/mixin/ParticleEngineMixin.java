package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    /**
     * Block break / mining / block dust particles are all TerrainParticle.
     * (ParticleEngine#destroy and #crack are not found in 1.21.11, so we filter here instead.)
     */
    @Inject(method = "add", at = @At("HEAD"), cancellable = true, require = 0)
    private void ool$noTerrainParticles(Particle particle, CallbackInfo ci) {
        if (VisualsConfig.BREAK_PARTICLES.value && particle instanceof TerrainParticle) {
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
