package dev.ool.visuals.mixin;

import dev.ool.visuals.VisualsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fast Block Place. Vanilla already repeats placing while the use button is held,
 * but waits 4 ticks between blocks (rightClickDelay). This lowers that wait,
 * only while a block is in a hand (pearls, buckets, food etc. keep the normal delay).
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    private int rightClickDelay;

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void ool$fastPlace(CallbackInfo ci) {
        if (!VisualsConfig.fastPlace) {
            return;
        }
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.player == null || mc.screen != null) {
            return;
        }
        boolean holdingBlock = mc.player.getMainHandItem().getItem() instanceof BlockItem
                || mc.player.getOffhandItem().getItem() instanceof BlockItem;
        if (holdingBlock && this.rightClickDelay > VisualsConfig.fastPlaceDelay) {
            this.rightClickDelay = VisualsConfig.fastPlaceDelay;
        }
    }
}
