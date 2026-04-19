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

    private static final int ECHO_ACTIVE_TICKS = 100;      // 5 seconds
    private static final int ECHO_COOLDOWN_TICKS = 600;    // 30 seconds

    /**
     * Effect #7 — grant Speed I to the player after a successful block with a T2+ shield.
     * Also delegates to {@link #tryGenerateEcho} for Effect #12 (T3 only) so both effects can
     * fire from the same block-success event. Internal cooldowns live in PlayerProgressData
     * to prevent shield-swap exploits.
     */
    public static void onBlockSuccess(net.minecraft.server.level.ServerPlayer player, ChronoShieldTier tier) {
        if (tier.hasSpeedOnBlock) {
            long now = player.level().getGameTime();
            com.chronodawn.data.PlayerProgressData data =
                com.chronodawn.data.PlayerProgressData.get((net.minecraft.server.level.ServerLevel) player.level());
            long cdEnd = data.getShieldSpeedCooldownEnd(player.getUUID());
            if (now >= cdEnd) {
                data.setShieldSpeedCooldownEnd(player.getUUID(), now + SPEED_COOLDOWN_TICKS);
                player.addEffect(new MobEffectInstance(
                    MobEffects.SPEED,
                    SPEED_DURATION_TICKS, 0, false, false, true));
            }
        }

        // Effect #12 — attempt echo generation (T3 only; tier-gated inside)
        tryGenerateEcho(player, tier);
    }

    /**
     * Effect #12 — schedule a ghost-shield echo after a successful block with a T3 shield.
     * Respects 30-second cooldown; no-op if an echo is already active (prevents refresh).
     */
    public static void tryGenerateEcho(net.minecraft.server.level.ServerPlayer player, ChronoShieldTier tier) {
        if (!tier.hasTimeEcho) return;
        long now = player.level().getGameTime();
        com.chronodawn.data.PlayerProgressData data =
            com.chronodawn.data.PlayerProgressData.get((net.minecraft.server.level.ServerLevel) player.level());
        long cdEnd = data.getShieldEchoCooldownEnd(player.getUUID());
        if (now < cdEnd) return;
        long activeUntil = data.getShieldEchoActiveUntil(player.getUUID());
        if (now < activeUntil) return;  // already active — don't refresh
        data.setShieldEchoActiveUntil(player.getUUID(), now + ECHO_ACTIVE_TICKS);
        // Particle / sound emission is Phase 7; intentionally omitted here.
    }

    /**
     * Effect #12 — consume an active echo to auto-block an incoming hit.
     * Returns true if the echo absorbed the damage (caller cancels the damage event).
     * Locates the held ChronoDawn shield (main or off hand) and deducts 1 durability.
     */
    public static boolean tryConsumeEcho(net.minecraft.server.level.ServerPlayer player) {
        long now = player.level().getGameTime();
        com.chronodawn.data.PlayerProgressData data =
            com.chronodawn.data.PlayerProgressData.get((net.minecraft.server.level.ServerLevel) player.level());
        long activeUntil = data.getShieldEchoActiveUntil(player.getUUID());
        if (now >= activeUntil) return false;  // no echo active
        // Consume: clear active window and start cooldown.
        data.setShieldEchoActiveUntil(player.getUUID(), 0L);
        data.setShieldEchoCooldownEnd(player.getUUID(), now + ECHO_COOLDOWN_TICKS);
        // Deduct 1 durability from whichever ChronoDawn shield is held.
        for (net.minecraft.world.InteractionHand hand : net.minecraft.world.InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ChronoShieldMarker) {
                stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.OFFHAND);
                break;
            }
        }
        return true;
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
