package com.chronodawn.items.shield;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class ChronoShieldEffectHandler {
    private ChronoShieldEffectHandler() {}

    private static final Set<Identifier> SHORTENED_EFFECTS = Set.of(
        BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.SLOWNESS.value()),
        BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.WEAKNESS.value()),
        BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.MINING_FATIGUE.value())
    );

    private static final int SPEED_DURATION_TICKS = 20;   // 1 second
    private static final int SPEED_COOLDOWN_TICKS = 60;   // 3 seconds

    /**
     * Effect #7 — grant Speed I to the player after a successful block with a T2+ shield.
     * Internal 3-second cooldown is stored in PlayerProgressData to prevent shield-swap exploits.
     * No-op for T1 (tier flag gates it).
     */
    public static void onBlockSuccess(net.minecraft.server.level.ServerPlayer player, ChronoShieldTier tier) {
        if (!tier.hasSpeedOnBlock) return;
        long now = player.level().getGameTime();
        com.chronodawn.data.PlayerProgressData data =
            com.chronodawn.data.PlayerProgressData.get((net.minecraft.server.level.ServerLevel) player.level());
        long cdEnd = data.getShieldSpeedCooldownEnd(player.getUUID());
        if (now < cdEnd) return;
        data.setShieldSpeedCooldownEnd(player.getUUID(), now + SPEED_COOLDOWN_TICKS);
        player.addEffect(new MobEffectInstance(
            MobEffects.SPEED,
            SPEED_DURATION_TICKS, 0, false, false, true));
    }

    /**
     * Effect A — halves duration of time-themed debuffs when the entity is holding a ChronoDawn shield
     * in either hand (main-hand or off-hand). Blocking is not required: the trigger is passive so
     * that unblockable sources (splash potions, lingering clouds, mob auras, environmental debuffs)
     * are mitigated too. Returns the (possibly modified) instance; a new instance is returned when
     * duration changes.
     */
    public static MobEffectInstance maybeShortenDebuff(LivingEntity target, MobEffectInstance incoming) {
        if (!isHoldingChronoShield(target)) return incoming;
        Holder<MobEffect> holder = incoming.getEffect();
        Identifier id = BuiltInRegistries.MOB_EFFECT.getKey(holder.value());
        if (!SHORTENED_EFFECTS.contains(id)) return incoming;
        int halved = Math.max(1, incoming.getDuration() / 2);
        return new MobEffectInstance(holder, halved, incoming.getAmplifier(), incoming.isAmbient(), incoming.isVisible(), incoming.showIcon());
    }

    /**
     * True when the entity is holding a ChronoDawn shield in either the main hand or off hand.
     * Used by Effect A (passive debuff shortening).
     */
    public static boolean isHoldingChronoShield(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof ChronoShieldMarker
            || entity.getOffhandItem().getItem() instanceof ChronoShieldMarker;
    }

    /**
     * True when the entity is actively blocking (use-item active) with a ChronoDawn shield.
     * Retained for effects whose trigger requires the shield to be raised (e.g. Speed on block, Time Echo).
     */
    public static boolean isBlockingWithChronoShield(LivingEntity entity) {
        if (!entity.isBlocking()) return false;
        ItemStack useItem = entity.getUseItem();
        return useItem.getItem() instanceof ChronoShieldMarker;
    }
}
