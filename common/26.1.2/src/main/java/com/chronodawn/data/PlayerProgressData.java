package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

/**
 * World saved data for player progress.
 *
 * This is the 1.21.5-specific version with single-parameter load method
 * and updated NBT API (getCompoundOrEmpty, keySet, getBooleanOr, getListOrEmpty).
 *
 * This class manages the persistent state of player-specific progress:
 * - Whether the player has obtained the Eye of Chronos
 * - List of stabilized portal IDs
 * - List of defeated bosses
 */
public class PlayerProgressData extends ChronoDawnWorldData {
    private static final String DATA_NAME = ChronoDawn.MOD_ID + "_player_progress";

    /**
     * Player progress entry.
     */
    public static class PlayerProgress {
        public boolean hasChronosEye;
        public Set<UUID> stabilizedPortals;
        public Set<String> defeatedBosses;

        // Shield state
        public long shieldSpeedCooldownEndTick;   // effect #7
        public long shieldEchoActiveUntilTick;    // effect #12
        public long shieldEchoCooldownEndTick;    // effect #12

        public PlayerProgress() {
            this.hasChronosEye = false;
            this.stabilizedPortals = new HashSet<>();
            this.defeatedBosses = new HashSet<>();
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
            progress.hasChronosEye = playerTag.getBooleanOr("has_chronos_eye", false);

            ListTag portalList = playerTag.getListOrEmpty("stabilized_portals");
            for (int i = 0; i < portalList.size(); i++) {
                portalList.getString(i).ifPresent(str -> {
                    try {
                        progress.stabilizedPortals.add(UUID.fromString(str));
                    } catch (IllegalArgumentException e) {
                        ChronoDawn.LOGGER.warn("Invalid portal UUID in progress data: {}", str);
                    }
                });
            }

            ListTag bossList = playerTag.getListOrEmpty("defeated_bosses");
            for (int i = 0; i < bossList.size(); i++) {
                bossList.getString(i).ifPresent(progress.defeatedBosses::add);
            }

            progress.shieldSpeedCooldownEndTick = playerTag.getLongOr("shield_speed_cd", 0L);
            progress.shieldEchoActiveUntilTick = playerTag.getLongOr("shield_echo_until", 0L);
            progress.shieldEchoCooldownEndTick = playerTag.getLongOr("shield_echo_cd", 0L);

            data.playerData.put(playerId, progress);

            ChronoDawn.LOGGER.debug("  Loaded player {}: hasChronosEye={}", playerId, progress.hasChronosEye);
        }

        return data;
    }

    @Override
    public CompoundTag saveData(CompoundTag tag) {
        CompoundTag playersTag = new CompoundTag();

        for (Map.Entry<UUID, PlayerProgress> entry : playerData.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            PlayerProgress progress = entry.getValue();

            playerTag.putBoolean("has_chronos_eye", progress.hasChronosEye);

            ListTag portalList = new ListTag();
            for (UUID portalId : progress.stabilizedPortals) {
                portalList.add(StringTag.valueOf(portalId.toString()));
            }
            playerTag.put("stabilized_portals", portalList);

            ListTag bossList = new ListTag();
            for (String bossName : progress.defeatedBosses) {
                bossList.add(StringTag.valueOf(bossName));
            }
            playerTag.put("defeated_bosses", bossList);

            playerTag.putLong("shield_speed_cd", progress.shieldSpeedCooldownEndTick);
            playerTag.putLong("shield_echo_until", progress.shieldEchoActiveUntilTick);
            playerTag.putLong("shield_echo_cd", progress.shieldEchoCooldownEndTick);

            playersTag.put(entry.getKey().toString(), playerTag);
        }

        tag.put("players", playersTag);

        ChronoDawn.LOGGER.debug("PlayerProgressData saved: {} players", playerData.size());
        for (Map.Entry<UUID, PlayerProgress> entry : playerData.entrySet()) {
            ChronoDawn.LOGGER.debug("  Player {}: hasChronosEye={}",
                entry.getKey(), entry.getValue().hasChronosEye);
        }

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
            progress.hasChronosEye = playerTag.getBooleanOr("has_chronos_eye", false);

            ListTag portalList = playerTag.getListOrEmpty("stabilized_portals");
            for (int i = 0; i < portalList.size(); i++) {
                portalList.getString(i).ifPresent(str -> {
                    try {
                        progress.stabilizedPortals.add(UUID.fromString(str));
                    } catch (IllegalArgumentException e) {
                        ChronoDawn.LOGGER.warn("Invalid portal UUID in progress data: {}", str);
                    }
                });
            }

            ListTag bossList = playerTag.getListOrEmpty("defeated_bosses");
            for (int i = 0; i < bossList.size(); i++) {
                bossList.getString(i).ifPresent(progress.defeatedBosses::add);
            }

            progress.shieldSpeedCooldownEndTick = playerTag.getLongOr("shield_speed_cd", 0L);
            progress.shieldEchoActiveUntilTick = playerTag.getLongOr("shield_echo_until", 0L);
            progress.shieldEchoCooldownEndTick = playerTag.getLongOr("shield_echo_cd", 0L);

            playerData.put(playerId, progress);
        }
    }

    /**
     * Get player progress for the given player ID.
     * Creates a new progress entry if it doesn't exist.
     *
     * @param playerId Player UUID
     * @return Player progress data
     */
    public PlayerProgress getProgress(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> new PlayerProgress());
    }

    /**
     * Check if the player has any stabilized portals.
     *
     * @param playerId Player UUID
     * @return True if the player has stabilized at least one portal
     */
    public boolean hasStabilizedPortals(UUID playerId) {
        PlayerProgress progress = playerData.get(playerId);
        return progress != null && !progress.stabilizedPortals.isEmpty();
    }

    /**
     * Set whether the player has obtained the Eye of Chronos.
     *
     * @param playerId Player UUID
     * @param hasEye Whether the player has the Eye of Chronos
     */
    public void setChronosEye(UUID playerId, boolean hasEye) {
        PlayerProgress progress = getProgress(playerId);
        progress.hasChronosEye = hasEye;
        setDirty();
    }

    /**
     * Add a stabilized portal to the player's progress.
     *
     * @param playerId Player UUID
     * @param portalId Portal UUID
     */
    public void addStabilizedPortal(UUID playerId, UUID portalId) {
        PlayerProgress progress = getProgress(playerId);
        progress.stabilizedPortals.add(portalId);
        setDirty();
    }

    /**
     * Add a defeated boss to the player's progress.
     *
     * @param playerId Player UUID
     * @param bossName Boss name
     */
    public void addDefeatedBoss(UUID playerId, String bossName) {
        PlayerProgress progress = getProgress(playerId);
        progress.defeatedBosses.add(bossName);
        setDirty();
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
