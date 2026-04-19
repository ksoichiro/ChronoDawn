package com.chronodawn.mixin;

import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import com.chronodawn.items.shield.ChronoShieldMarker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Effect #7 / #12 block-success detection for Era C (MC 1.20.1).
 *
 * <p>Era C predates both the {@code BLOCKS_ATTACKS} component (1.21.5+) and the
 * {@code applyItemBlocking} method used by Era A; it shares with Era B the
 * strategy of piggy-backing on {@link LivingEntity#hurtCurrentlyUsedShield(float)},
 * which vanilla calls once per shield-absorbed hit. This gives us a reliable
 * "shield just absorbed damage" signal plus direct access to the shield stack
 * via {@link LivingEntity#getUseItem()}.</p>
 *
 * <p>The handler is a no-op unless the use-item is a {@link ChronoShieldMarker}, so
 * vanilla shields and other mods' shields are untouched.</p>
 */
@Mixin(LivingEntity.class)
public abstract class ChronoShieldBlockingMixin {

    @Inject(
        method = "hurtCurrentlyUsedShield(F)V",
        at = @At("HEAD")
    )
    private void chronodawn$onShieldBlockSuccess(float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer sp)) return;
        ItemStack useItem = self.getUseItem();
        if (!(useItem.getItem() instanceof ChronoShieldMarker marker)) return;
        ChronoShieldEffectHandler.onBlockSuccess(sp, marker.getChronoShieldTier());
    }
}
