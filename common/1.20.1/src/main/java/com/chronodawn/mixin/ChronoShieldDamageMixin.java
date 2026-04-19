package com.chronodawn.mixin;

import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Effect #12 — Time Echo: when a ghost-shield echo is active (5 seconds after a T3
 * successful block), the next incoming shield-blockable hit is auto-absorbed even
 * though the player is not actively raising a shield.
 *
 * <p>MC 1.20.1 uses {@link Player#hurt(DamageSource, float)} as the damage entry
 * (same as 1.21.1; {@code hurtServer(ServerLevel, DamageSource, float)} is 1.21.2+).
 * DamageTypeTags already exist in 1.20.1 via the vanilla damage type tag registry.</p>
 *
 * <p>Return-value semantics: vanilla {@code hurt} returns {@code false} when damage
 * did not apply. When the echo absorbs the hit, we set the return value to {@code false}
 * so the surrounding pipeline short-circuits like a dodge.</p>
 */
@Mixin(Player.class)
public abstract class ChronoShieldDamageMixin {

    @Inject(
        method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void chronodawn$maybeConsumeEcho(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        if (!(self instanceof ServerPlayer sp)) return;
        // Server-side only: clients receive damage via client-predicted path that
        // doesn't invoke this method the same way.
        if (!(sp.level() instanceof ServerLevel)) return;
        // Vanilla shield path handles blocked hits for raised shields — don't double-consume.
        if (self.isBlocking()) return;
        if (!isEchoBlockable(source)) return;
        if (ChronoShieldEffectHandler.tryConsumeEcho(sp)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Mirrors vanilla's shield-blockable filter. Echo should only absorb hits a
     * raised shield could have absorbed in the first place.
     */
    private static boolean isEchoBlockable(DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_SHIELD)) return false;
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) return false;
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        if (source.is(DamageTypeTags.IS_FIRE)) return false;
        if (source.is(DamageTypeTags.IS_FALL)) return false;
        if (source.is(DamageTypeTags.IS_DROWNING)) return false;
        return true;
    }
}
