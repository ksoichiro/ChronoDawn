package com.chronodawn.mixin;

import com.chronodawn.items.shield.ChronoShieldEffectHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Effect A — shortens time-themed debuffs (Slowness / Weakness / Mining Fatigue) applied to
 * entities holding a ChronoDawn shield or wearing a full Clockstone / Enhanced Clockstone armor set.
 *
 * Hooks {@link LivingEntity#addEffect(MobEffectInstance, net.minecraft.world.entity.Entity)}
 * at HEAD and rewrites the incoming {@link MobEffectInstance} argument via
 * {@link ChronoShieldEffectHandler#maybeShortenDebuff}. The handler itself is a no-op unless
 * the target holds a {@code ChronoShieldMarker} item or wears one of those armor sets, so
 * non-Chrono shields and unrelated entities are unaffected.
 */
@Mixin(LivingEntity.class)
public abstract class ChronoShieldEffectInterceptorMixin {

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
                    at = @At("HEAD"),
                    argsOnly = true,
                    ordinal = 0)
    private MobEffectInstance chronodawn$shortenShieldDebuff(MobEffectInstance incoming) {
        LivingEntity self = (LivingEntity)(Object)this;
        return ChronoShieldEffectHandler.maybeShortenDebuff(self, incoming);
    }
}
