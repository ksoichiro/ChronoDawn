package com.chronodawn.mixin;

import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import com.chronodawn.items.shield.ChronoShieldMarker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Effect #7 — grants Speed I to the player immediately after a successful block
 * with a T2+ ChronoDawn shield (internal 3-second cooldown lives in the handler).
 *
 * <p>Targets {@code LivingEntity#applyItemBlocking(ServerLevel, DamageSource, float)}
 * (MC 1.21.5+ API introduced with the {@code BLOCKS_ATTACKS} component). The returned
 * float is the amount of
 * damage absorbed by the shield — strictly {@code > 0} only when the shield actually
 * mitigated damage. This gives a precise "block succeeded" signal without having to
 * chase individual call sites inside the damage pipeline.</p>
 *
 * <p>We intentionally read the currently-blocking stack via {@link LivingEntity#getUseItem()}
 * rather than the main/off hand: {@code applyItemBlocking} is invoked while {@code useItem}
 * still points at the raised shield, and the handler is a no-op unless the tier has
 * {@code hasSpeedOnBlock}, so non-T2+ shields (and non-Chrono shields) cost nothing.</p>
 */
@Mixin(LivingEntity.class)
public abstract class ChronoShieldBlockingMixin {

    @Inject(
        method = "applyItemBlocking(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)F",
        at = @At("RETURN")
    )
    private void chronodawn$onShieldBlockSuccess(
            ServerLevel level,
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Float> cir) {
        Float blocked = cir.getReturnValue();
        if (blocked == null || blocked <= 0.0F) {
            return; // shield did not absorb anything (e.g. bypassed, wrong angle, no component)
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer sp)) return;
        ItemStack useItem = self.getUseItem();
        if (!(useItem.getItem() instanceof ChronoShieldMarker marker)) return;
        ChronoShieldEffectHandler.onBlockSuccess(sp, marker.getChronoShieldTier());
    }
}
