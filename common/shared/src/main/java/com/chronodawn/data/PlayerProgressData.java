package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * World saved data for player progress.
 *
 * This class manages the persistent per-player Chrono Shield effect state
 * (cooldowns and active windows).
 *
 * Reference: data-model.md (Data Persistence - Player Progress)
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
     *
     * @param tag CompoundTag to read from
     * @param registries Registry access for deserialization
     * @return Loaded player progress data instance
     */
    private static PlayerProgressData load(CompoundTag tag, HolderLookup.Provider registries) {
        PlayerProgressData data = new PlayerProgressData();
        CompoundTag playersTag = tag.getCompound("players");

        ChronoDawn.LOGGER.debug("PlayerProgressData loading: {} players in NBT", playersTag.getAllKeys().size());

        for (String key : playersTag.getAllKeys()) {
            UUID playerId = UUID.fromString(key);
            CompoundTag playerTag = playersTag.getCompound(key);

            PlayerProgress progress = new PlayerProgress();
            progress.shieldSpeedCooldownEndTick = playerTag.getLong("shield_speed_cd");
            progress.shieldEchoActiveUntilTick = playerTag.getLong("shield_echo_until");
            progress.shieldEchoCooldownEndTick = playerTag.getLong("shield_echo_cd");

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
        CompoundTag playersTag = tag.getCompound("players");

        for (String key : playersTag.getAllKeys()) {
            UUID playerId = UUID.fromString(key);
            CompoundTag playerTag = playersTag.getCompound(key);

            PlayerProgress progress = new PlayerProgress();
            progress.shieldSpeedCooldownEndTick = playerTag.getLong("shield_speed_cd");
            progress.shieldEchoActiveUntilTick = playerTag.getLong("shield_echo_until");
            progress.shieldEchoCooldownEndTick = playerTag.getLong("shield_echo_cd");

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
