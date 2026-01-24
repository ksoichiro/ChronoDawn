package com.chronodawn.core.time;

import com.chronodawn.ChronoDawn;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Reversed Resonance (反転共鳴) - Time Distortion Reversal Effect
 *
 * A dangerous phenomenon where the normal time distortion effects are reversed:
 * - Players receive Slowness IV
 * - Nearby hostile mobs receive Speed II
 *
 * Triggers (from data-model.md):
 * 1. Crafting Unstable Hourglass (30 seconds duration)
 * 2. Defeating Time Guardian (30 seconds duration)
 * 3. Defeating Time Tyrant (60 seconds duration)
 *
 * Effect Details:
 * - Player: Slowness IV (duration varies by trigger)
 * - Mobs: Speed II (same duration) within 20-block radius
 * - Warning message displayed before activation
 *
 * Reference: data-model.md (Reversed Resonance)
 * Task: T115 [US2] Implement reversed resonance trigger on Time Guardian defeat
 */
public class ReversedResonance {
    // Effect durations (in ticks, 20 ticks = 1 second)
    public static final int DURATION_SHORT = 600;  // 30 seconds (Unstable Hourglass, Time Guardian)
    public static final int DURATION_LONG = 1200;  // 60 seconds (Time Tyrant)

    // Effect range (in blocks)
    public static final double EFFECT_RANGE = 20.0;

    /**
     * Trigger reversed resonance effect at the specified location.
     *
     * @param level The ServerLevel where the effect occurs
     * @param center The center position of the effect
     * @param durationTicks Duration of the effect in ticks
     * @param triggerSource Source description for logging (e.g., "Time Guardian defeat")
     */
    public static void trigger(ServerLevel level, Vec3 center, int durationTicks, String triggerSource) {
        ChronoDawn.LOGGER.info(
            "Reversed Resonance triggered at [{}, {}, {}] for {} ticks (source: {})",
            center.x, center.y, center.z, durationTicks, triggerSource
        );

        // Get all entities within range
        AABB effectArea = new AABB(center, center).inflate(EFFECT_RANGE);

        // Apply effects to players
        level.getEntitiesOfClass(Player.class, effectArea).forEach(player -> {
            applyPlayerEffect(player, durationTicks);
        });

        // Apply effects to hostile mobs (excluding players)
        level.getEntitiesOfClass(LivingEntity.class, effectArea).forEach(entity -> {
            // Only apply to hostile mobs, not players
            if (!(entity instanceof Player) && entity.getType().getCategory().isFriendly() == false) {
                applyMobEffect(entity, durationTicks);
            }
        });
    }

    /**
     * Apply Slowness IV effect to a player with warning message.
     *
     * @param player The player to affect
     * @param durationTicks Duration in ticks
     */
    private static void applyPlayerEffect(Player player, int durationTicks) {
        // Show warning message
        if (player instanceof ServerPlayer serverPlayer) {
            Component warningMessage = Component.literal("⚠ 反転共鳴 / Reversed Resonance ⚠")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

            serverPlayer.displayClientMessage(warningMessage, false);

            // Send title overlay
            Component title = Component.literal("反転共鳴")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
            Component subtitle = Component.literal("Reversed Resonance")
                .withStyle(ChatFormatting.RED);

            serverPlayer.connection.send(
                new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(
                    10, // fade in
                    40, // stay
                    10  // fade out
                )
            );

            serverPlayer.connection.send(
                new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title)
            );

            serverPlayer.connection.send(
                new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitle)
            );
        }

        // Apply Slowness IV
        MobEffectInstance slowness = new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            durationTicks,
            3, // Slowness IV (amplifier 3)
            false,
            true,
            true
        );

        player.addEffect(slowness);

        ChronoDawn.LOGGER.debug(
            "Applied Slowness IV to player {} for {} ticks",
            player.getName().getString(),
            durationTicks
        );
    }

    /**
     * Apply Speed II effect to a hostile mob.
     *
     * @param entity The mob to affect
     * @param durationTicks Duration in ticks
     */
    private static void applyMobEffect(LivingEntity entity, int durationTicks) {
        // Apply Speed II
        MobEffectInstance speed = new MobEffectInstance(
            MobEffects.MOVEMENT_SPEED,
            durationTicks,
            1, // Speed II (amplifier 1)
            false,
            true,
            true
        );

        entity.addEffect(speed);

        ChronoDawn.LOGGER.debug(
            "Applied Speed II to mob {} for {} ticks",
            entity.getType().getDescriptionId(),
            durationTicks
        );
    }

    /**
     * Trigger reversed resonance on Time Guardian defeat (30 seconds).
     *
     * @param level The ServerLevel where the defeat occurred
     * @param center The position of the defeated Time Guardian
     */
    public static void triggerOnTimeGuardianDefeat(ServerLevel level, Vec3 center) {
        trigger(level, center, DURATION_SHORT, "Time Guardian defeat");
    }

    /**
     * Trigger reversed resonance on Time Tyrant defeat (60 seconds).
     *
     * @param level The ServerLevel where the defeat occurred
     * @param center The position of the defeated Time Tyrant
     */
    public static void triggerOnTimeTyrantDefeat(ServerLevel level, Vec3 center) {
        trigger(level, center, DURATION_LONG, "Time Tyrant defeat");
    }

    /**
     * Trigger reversed resonance on Unstable Hourglass crafting (30 seconds).
     *
     * @param level The ServerLevel where the crafting occurred
     * @param center The position of the player who crafted the item
     */
    public static void triggerOnUnstableHourglassCraft(ServerLevel level, Vec3 center) {
        trigger(level, center, DURATION_SHORT, "Unstable Hourglass craft");
    }
}
