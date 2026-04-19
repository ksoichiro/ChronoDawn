package com.chronodawn.items.shield;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Era B variant (MC 1.21.1 / 1.21.2 / 1.21.3 / 1.21.4) — these MC versions use the OLD
 * {@link MobEffects} constant names (MOVEMENT_SLOWDOWN / DIG_SLOWDOWN / MOVEMENT_SPEED)
 * instead of the SLOWNESS / MINING_FATIGUE / SPEED names introduced in later releases.
 * The shared copy (at common/shared/) uses the new names for Era A; this per-version
 * copy is selected by {@code common/<ver>/build.gradle}'s {@code java.filter.exclude}.
 */
public final class ChronoShieldEffectHandler {
    private ChronoShieldEffectHandler() {}

    private static final Set<ResourceLocation> SHORTENED_EFFECTS = Set.of(
        BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.MOVEMENT_SLOWDOWN.value()),
        BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.WEAKNESS.value()),
        BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.DIG_SLOWDOWN.value())
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
                    MobEffects.MOVEMENT_SPEED,
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

        // Stage 1: ring burst + sound. Guarded so unit tests with mock levels don't
        // trip on unpopulated registry suppliers or unstubbed mock methods.
        emitEchoGenerateFx(player);
    }

    /**
     * Stage 1 FX — 8-particle ring burst around the player plus a generate sound.
     * Extracted and guarded so unit tests with mocked ServerLevel / unregistered
     * ModParticles / ModSounds suppliers don't fail inside the state-only handler.
     */
    private static void emitEchoGenerateFx(net.minecraft.server.level.ServerPlayer player) {
        try {
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
            for (int i = 0; i < 8; i++) {
                double angle = (Math.PI * 2.0 * i) / 8.0;
                double dx = Math.cos(angle) * 0.8;
                double dz = Math.sin(angle) * 0.8;
                level.sendParticles(
                    com.chronodawn.registry.ModParticles.CHRONO_SHIELD_ECHO.get(),
                    player.getX() + dx, player.getY() + 1.2, player.getZ() + dz,
                    1, 0, 0, 0, 0
                );
            }
            level.playSound(
                null, player.blockPosition(),
                com.chronodawn.registry.ModSounds.SHIELD_ECHO_GENERATE.get(),
                net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.2f
            );
        } catch (Throwable ignored) {
            // FX emission is best-effort; state changes above are authoritative.
        }
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

        // Stage 3: consumption burst + sound. Guarded against unit-test mocks.
        emitEchoConsumeFx(player);
        return true;
    }

    /**
     * Stage 3 FX — 12-particle outward burst plus a consume sound.
     * Guarded so unit tests with mocked ServerLevel don't fail on unstubbed
     * getRandom() / unregistered ModParticles or ModSounds suppliers.
     */
    private static void emitEchoConsumeFx(net.minecraft.server.level.ServerPlayer player) {
        try {
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
            net.minecraft.util.RandomSource random = level.getRandom();
            for (int i = 0; i < 12; i++) {
                double dx = (random.nextDouble() - 0.5) * 1.5;
                double dy = random.nextDouble() * 1.0;
                double dz = (random.nextDouble() - 0.5) * 1.5;
                level.sendParticles(
                    com.chronodawn.registry.ModParticles.CHRONO_SHIELD_ECHO.get(),
                    player.getX() + dx, player.getY() + dy, player.getZ() + dz,
                    1, 0, 0.05, 0, 0.02
                );
            }
            level.playSound(
                null, player.blockPosition(),
                com.chronodawn.registry.ModSounds.SHIELD_ECHO_CONSUME.get(),
                net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 0.9f
            );
        } catch (Throwable ignored) {
            // FX emission is best-effort; state changes above are authoritative.
        }
    }

    /**
     * Stage 2 FX — periodic drift particle while an echo is active.
     * Called from {@link com.chronodawn.events.ChronoShieldTickHandler} once every
     * 10 ticks for each player whose echo is still active. Guarded against
     * mocked ServerLevel calls so test scaffolding stays lightweight.
     */
    public static void emitEchoActiveDriftFx(net.minecraft.server.level.ServerPlayer player) {
        try {
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
            level.sendParticles(
                com.chronodawn.registry.ModParticles.CHRONO_SHIELD_ECHO.get(),
                player.getX(), player.getY() + 1.0, player.getZ(),
                1,
                0.3, 0.3, 0.3,   // offset spread
                0.01             // velocity
            );
        } catch (Throwable ignored) {
            // FX emission is best-effort.
        }
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
        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(holder.value());
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
