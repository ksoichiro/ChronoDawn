package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

/**
 * World saved data for player progress.
 *
 * This is the 1.21.5-specific version with single-parameter load method
 * and updated NBT API (getCompoundOrEmpty, keySet, getLongOr).
 *
 * This class manages the persistent per-player Chrono Shield effect state
 * (cooldowns and active windows).
 */
public class PlayerProgressData extends ChronoDawnWorldData {
    private static final String DATA_NAME = ChronoDawn.MOD_ID + "_player_progress";

    /**
     * Player progress entry.
     */
    public static class PlayerProgress {
        // Shield state
        public long shieldSpeedCooldownEndTick;   // effect #7
        public long shieldEchoActiveUntilTick;    // effect #12
        public long shieldEchoCooldownEndTick;    // effect #12

        public PlayerProgress() {
            this.shieldSpeedCooldownEndTick = 0L;
            this.shieldEchoActiveUntilTick = 0L;
            this.shieldEchoCooldownEndTick = 0L;
        }
    }

    private final Map<UUID, PlayerProgress> playerData = new HashMap<>();

    /**
     * Get or create player progress data for the given level.
     *
     * @param level ServerLevel to get data from
     * @return Player progress data instance
     */
    public static PlayerProgressData get(ServerLevel level) {
        return CompatSavedData.computeIfAbsent(
            level.getDataStorage(),
            PlayerProgressData::new,
            PlayerProgressData::load,
            DATA_NAME
        );
    }

    /**
     * Load player progress data from NBT.
     * In 1.21.5, the load method no longer needs HolderLookup.Provider.
     *
     * @param tag CompoundTag to read from
     * @return Loaded player progress data instance
     */
    private static PlayerProgressData load(CompoundTag tag) {
        PlayerProgressData data = new PlayerProgressData();
        CompoundTag playersTag = tag.getCompoundOrEmpty("players");

        ChronoDawn.LOGGER.debug("PlayerProgressData loading: {} players in NBT", playersTag.keySet().size());

        for (String key : playersTag.keySet()) {
            UUID playerId;
            try {
                playerId = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                ChronoDawn.LOGGER.warn("Invalid player UUID in progress data: {}", key);
                continue;
            }

            CompoundTag playerTag = playersTag.getCompoundOrEmpty(key);

            PlayerProgress progress = new PlayerProgress();
            progress.shieldSpeedCooldownEndTick = playerTag.getLongOr("shield_speed_cd", 0L);
            progress.shieldEchoActiveUntilTick = playerTag.getLongOr("shield_echo_until", 0L);
            progress.shieldEchoCooldownEndTick = playerTag.getLongOr("shield_echo_cd", 0L);

            data.playerData.put(playerId, progress);
        }

        return data;
    }

    @Override
    public CompoundTag saveData(CompoundTag tag) {
        CompoundTag playersTag = new CompoundTag();

        for (Map.Entry<UUID, PlayerProgress> entry : playerData.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            PlayerProgress progress = entry.getValue();

            playerTag.putLong("shield_speed_cd", progress.shieldSpeedCooldownEndTick);
            playerTag.putLong("shield_echo_until", progress.shieldEchoActiveUntilTick);
            playerTag.putLong("shield_echo_cd", progress.shieldEchoCooldownEndTick);

            playersTag.put(entry.getKey().toString(), playerTag);
        }

        tag.put("players", playersTag);

        ChronoDawn.LOGGER.debug("PlayerProgressData saved: {} players", playerData.size());

        return tag;
    }

    @Override
    public void loadData(CompoundTag tag) {
        CompoundTag playersTag = tag.getCompoundOrEmpty("players");

        for (String key : playersTag.keySet()) {
            UUID playerId;
            try {
                playerId = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                ChronoDawn.LOGGER.warn("Invalid player UUID in progress data: {}", key);
                continue;
            }

            CompoundTag playerTag = playersTag.getCompoundOrEmpty(key);

            PlayerProgress progress = new PlayerProgress();
            progress.shieldSpeedCooldownEndTick = playerTag.getLongOr("shield_speed_cd", 0L);
            progress.shieldEchoActiveUntilTick = playerTag.getLongOr("shield_echo_until", 0L);
            progress.shieldEchoCooldownEndTick = playerTag.getLongOr("shield_echo_cd", 0L);

            playerData.put(playerId, progress);
        }
    }

    // --- Shield state accessors ---

    public long getShieldSpeedCooldownEnd(java.util.UUID playerId) {
        return playerData.getOrDefault(playerId, new PlayerProgress()).shieldSpeedCooldownEndTick;
    }

    public void setShieldSpeedCooldownEnd(java.util.UUID playerId, long endTick) {
        playerData.computeIfAbsent(playerId, k -> new PlayerProgress()).shieldSpeedCooldownEndTick = endTick;
        setDirty();
    }

    public long getShieldEchoActiveUntil(java.util.UUID playerId) {
        return playerData.getOrDefault(playerId, new PlayerProgress()).shieldEchoActiveUntilTick;
    }

    public void setShieldEchoActiveUntil(java.util.UUID playerId, long untilTick) {
        playerData.computeIfAbsent(playerId, k -> new PlayerProgress()).shieldEchoActiveUntilTick = untilTick;
        setDirty();
    }

    public long getShieldEchoCooldownEnd(java.util.UUID playerId) {
        return playerData.getOrDefault(playerId, new PlayerProgress()).shieldEchoCooldownEndTick;
    }

    public void setShieldEchoCooldownEnd(java.util.UUID playerId, long endTick) {
        playerData.computeIfAbsent(playerId, k -> new PlayerProgress()).shieldEchoCooldownEndTick = endTick;
        setDirty();
    }
}
