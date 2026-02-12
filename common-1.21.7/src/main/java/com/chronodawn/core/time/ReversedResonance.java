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
 * Reversed Resonance (反転共鳴) - Time Distortion Reversal Effect (1.21.5 version)
 *
 * A dangerous phenomenon where the normal time distortion effects are reversed:
 * - Players receive Slowness IV
 * - Nearby hostile mobs receive Speed II
 *
 * Note: In 1.21.5, MobEffects constants were renamed:
 * - MOVEMENT_SLOWDOWN → SLOWNESS
 * - MOVEMENT_SPEED → SPEED
 */
public class ReversedResonance {
    public static final int DURATION_SHORT = 600;  // 30 seconds
    public static final int DURATION_LONG = 1200;  // 60 seconds
    public static final double EFFECT_RANGE = 20.0;

    /**
     * Trigger reversed resonance effect at the specified location.
     */
    public static void trigger(ServerLevel level, Vec3 center, int durationTicks, String triggerSource) {
        ChronoDawn.LOGGER.debug(
            "Reversed Resonance triggered at [{}, {}, {}] for {} ticks (source: {})",
            center.x, center.y, center.z, durationTicks, triggerSource
        );

        AABB effectArea = new AABB(center, center).inflate(EFFECT_RANGE);

        // Apply effects to players
        level.getEntitiesOfClass(Player.class, effectArea).forEach(player -> {
            applyPlayerEffect(player, durationTicks);
        });

        // Apply effects to hostile mobs
        level.getEntitiesOfClass(LivingEntity.class, effectArea).forEach(entity -> {
            if (!(entity instanceof Player) && entity.getType().getCategory().isFriendly() == false) {
                applyMobEffect(entity, durationTicks);
            }
        });
    }

    /**
     * Apply Slowness IV effect to a player with warning message.
     */
    private static void applyPlayerEffect(Player player, int durationTicks) {
        if (player instanceof ServerPlayer serverPlayer) {
            Component warningMessage = Component.literal("⚠ 反転共鳴 / Reversed Resonance ⚠")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

            serverPlayer.displayClientMessage(warningMessage, false);

            Component title = Component.literal("反転共鳴")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
            Component subtitle = Component.literal("Reversed Resonance")
                .withStyle(ChatFormatting.RED);

            serverPlayer.connection.send(
                new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 40, 10)
            );

            serverPlayer.connection.send(
                new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title)
            );

            serverPlayer.connection.send(
                new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitle)
            );
        }

        // Apply Slowness IV - in 1.21.5, use SLOWNESS instead of MOVEMENT_SLOWDOWN
        MobEffectInstance slowness = new MobEffectInstance(
            MobEffects.SLOWNESS,
            durationTicks,
            3, // Slowness IV
            false,
            true,
            true
        );

        player.addEffect(slowness);
    }

    /**
     * Apply Speed II effect to a hostile mob.
     */
    private static void applyMobEffect(LivingEntity entity, int durationTicks) {
        // Apply Speed II - in 1.21.5, use SPEED instead of MOVEMENT_SPEED
        MobEffectInstance speed = new MobEffectInstance(
            MobEffects.SPEED,
            durationTicks,
            1, // Speed II
            false,
            true,
            true
        );

        entity.addEffect(speed);
    }

    public static void triggerOnTimeGuardianDefeat(ServerLevel level, Vec3 center) {
        trigger(level, center, DURATION_SHORT, "Time Guardian defeat");
    }

    public static void triggerOnTimeTyrantDefeat(ServerLevel level, Vec3 center) {
        trigger(level, center, DURATION_LONG, "Time Tyrant defeat");
    }

    public static void triggerOnUnstableHourglassCraft(ServerLevel level, Vec3 center) {
        trigger(level, center, DURATION_SHORT, "Unstable Hourglass craft");
    }
}
