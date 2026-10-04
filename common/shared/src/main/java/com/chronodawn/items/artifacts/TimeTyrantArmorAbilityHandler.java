package com.chronodawn.items.artifacts;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.misc.TemporalEchoDecoyEntity;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModItems;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Special abilities of the Time Tyrant armor pieces.
 *
 * <p>Time Tyrant's Mail - Temporal Rollback: when the wearer would die, 20% chance to
 * cancel the death and restore the health and position from about 3 seconds earlier.
 * 60 second cooldown. Like the Totem of Undying, it does not work against damage that
 * bypasses invulnerability (/kill, the void).
 *
 * <p>Echoing Time Boots - Temporal Echo: while the wearer sprints with mobs targeting
 * them, an afterimage decoy is left behind and those mobs switch to it for 3 seconds.
 * 15 second cooldown.
 *
 * <p>Cooldowns and history live in memory only, so a server restart resets them.
 */
public final class TimeTyrantArmorAbilityHandler {
    private TimeTyrantArmorAbilityHandler() {}

    public static final float ROLLBACK_CHANCE = 0.2f;
    public static final int ROLLBACK_WINDOW_TICKS = 60;
    public static final int ROLLBACK_COOLDOWN_TICKS = 1200;
    private static final int HISTORY_INTERVAL_TICKS = 5;

    public static final int ECHO_COOLDOWN_TICKS = 300;
    public static final double ECHO_LURE_RADIUS = 16.0;
    // The pursuer scan is an entity query, so it runs at the same cadence as history recording.
    private static final int ECHO_CHECK_INTERVAL_TICKS = 5;

    private static final Map<UUID, TemporalRollbackHistory<ResourceKey<Level>>> HISTORIES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> ROLLBACK_COOLDOWN_END = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> ECHO_COOLDOWN_END = new ConcurrentHashMap<>();

    public static void register() {
        TickEvent.PLAYER_POST.register(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                recordHistory(serverPlayer);
                tryTemporalEcho(serverPlayer);
            }
        });
        EntityEvent.LIVING_DEATH.register(TimeTyrantArmorAbilityHandler::onLivingDeath);
        PlayerEvent.PLAYER_QUIT.register(player -> {
            UUID uuid = player.getUUID();
            HISTORIES.remove(uuid);
            ROLLBACK_COOLDOWN_END.remove(uuid);
            ECHO_COOLDOWN_END.remove(uuid);
        });
        ChronoDawn.LOGGER.debug("Registered TimeTyrantArmorAbilityHandler");
    }

    public static boolean isCooldownReady(long now, Long cooldownEnd) {
        return cooldownEnd == null || now >= cooldownEnd;
    }

    private static boolean isWearingMail(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.TIME_TYRANT_MAIL.get());
    }

    private static boolean isWearingBoots(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ECHOING_TIME_BOOTS.get());
    }

    // === Temporal Rollback ===

    private static void recordHistory(ServerPlayer player) {
        if (!isWearingMail(player) || !player.isAlive()) {
            HISTORIES.remove(player.getUUID());
            return;
        }
        long now = player.level().getGameTime();
        if (now % HISTORY_INTERVAL_TICKS != 0) {
            return;
        }
        HISTORIES.computeIfAbsent(player.getUUID(), uuid -> new TemporalRollbackHistory<>(ROLLBACK_WINDOW_TICKS))
            .record(new TemporalRollbackHistory.Snapshot<>(
                now, player.level().dimension(),
                player.getX(), player.getY(), player.getZ(), player.getHealth()
            ));
    }

    private static EventResult onLivingDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayer player) || !isWearingMail(player)) {
            return EventResult.pass();
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return EventResult.pass();
        }
        long now = player.level().getGameTime();
        UUID uuid = player.getUUID();
        if (!isCooldownReady(now, ROLLBACK_COOLDOWN_END.get(uuid))) {
            return EventResult.pass();
        }
        if (player.getRandom().nextFloat() >= ROLLBACK_CHANCE) {
            return EventResult.pass();
        }
        TemporalRollbackHistory<ResourceKey<Level>> history = HISTORIES.get(uuid);
        Optional<TemporalRollbackHistory.Snapshot<ResourceKey<Level>>> target =
            history == null ? Optional.empty() : history.rewindTarget(now, player.level().dimension());
        if (target.isEmpty()) {
            return EventResult.pass();
        }

        rollback(player, target.get());
        ROLLBACK_COOLDOWN_END.put(uuid, now + ROLLBACK_COOLDOWN_TICKS);
        history.clear();
        return EventResult.interruptFalse();
    }

    private static void rollback(ServerPlayer player, TemporalRollbackHistory.Snapshot<ResourceKey<Level>> snapshot) {
        ServerLevel level = (ServerLevel) player.level();
        emitRollbackFx(level, player.getX(), player.getY(), player.getZ());

        player.setHealth(Math.max(1.0f, snapshot.health()));
        player.clearFire();
        player.resetFallDistance();
        player.stopRiding();
        // Drop the knockback or fall speed from the fatal hit; otherwise a rewind to a
        // mid-air snapshot of the same fall kills the player again. Zeroing it before the
        // teleport is enough: 1.21.2+ sends this velocity in the teleport packet, and older
        // clients clear velocity on an absolute teleport.
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(snapshot.x(), snapshot.y(), snapshot.z());

        emitRollbackFx(level, snapshot.x(), snapshot.y(), snapshot.z());
    }

    private static void emitRollbackFx(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 1.0, z, 40, 0.4, 0.8, 0.4, 0.1);
        level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.6f);
    }

    // === Temporal Echo ===

    private static void tryTemporalEcho(ServerPlayer player) {
        if (!player.isSprinting() || !player.isAlive() || player.isSpectator() || !isWearingBoots(player)) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();
        if (now % ECHO_CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        UUID uuid = player.getUUID();
        if (!isCooldownReady(now, ECHO_COOLDOWN_END.get(uuid))) {
            return;
        }

        // Only leave a decoy when something is actually chasing the wearer, so plain
        // sprinting doesn't burn the cooldown or spawn pointless entities.
        AABB area = player.getBoundingBox().inflate(ECHO_LURE_RADIUS);
        List<Mob> pursuers = level.getEntitiesOfClass(Mob.class, area, mob -> mob.getTarget() == player);
        if (pursuers.isEmpty()) {
            return;
        }

        TemporalEchoDecoyEntity decoy = new TemporalEchoDecoyEntity(ModEntities.TEMPORAL_ECHO_DECOY.get(), level);
        decoy.setPos(player.getX(), player.getY(), player.getZ());
        decoy.setYRot(player.getYRot());
        decoy.setYHeadRot(player.getYHeadRot());
        decoy.setYBodyRot(player.getYRot());
        if (!level.addFreshEntity(decoy)) {
            return;
        }
        for (Mob mob : pursuers) {
            mob.setTarget(decoy);
        }
        ECHO_COOLDOWN_END.put(uuid, now + ECHO_COOLDOWN_TICKS);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.8f, 1.2f);
    }
}
