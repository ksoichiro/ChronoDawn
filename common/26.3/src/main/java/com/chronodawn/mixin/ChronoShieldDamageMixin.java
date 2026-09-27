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
 * <p>Injects at HEAD of {@link Player#hurtServer(ServerLevel, DamageSource, float)}
 * (MC 1.21.5+ server-side damage entry point). Skipped when the player is already
 * actively raising a shield (vanilla shield path handles it) or when the damage
 * source bypasses shields (fall, suffocation, drowning, fire, magic bypass, etc.).</p>
 *
 * <p>Return-value semantics: vanilla {@code hurtServer} returns {@code false} when
 * damage did not apply to the entity (dead, invulnerable, fully blocked, dodged).
 * When the echo absorbs the hit, we set the return value to {@code false} so the
 * surrounding pipeline (hurt animation, knockback, sounds, stats) is short-circuited
 * exactly like a dodge. See {@code TemporalPhantomEntity#hurtServer} for the same
 * pattern.</p>
 */
@Mixin(Player.class)
public abstract class ChronoShieldDamageMixin {

    @Inject(
        method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void chronodawn$maybeConsumeEcho(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        if (!(self instanceof ServerPlayer sp)) return;
        // Vanilla shield path handles blocked hits for raised shields — don't double-consume.
        if (self.isBlocking()) return;
        if (!isEchoBlockable(source)) return;
        if (ChronoShieldEffectHandler.tryConsumeEcho(sp)) {
            // false = damage did not apply (matches vanilla "fully blocked" return).
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
