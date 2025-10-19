package com.chronosphere.data;

import com.chronosphere.Chronosphere;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * World saved data for player progress.
 *
 * This class manages the persistent state of player-specific progress:
 * - Whether the player has obtained the Eye of Chronos
 * - List of stabilized portal IDs
 * - List of defeated bosses
 *
 * Reference: data-model.md (Data Persistence - Player Progress)
 */
public class PlayerProgressData extends ChronosphereWorldData {
    private static final String DATA_NAME = Chronosphere.MOD_ID + "_player_progress";

    /**
     * Player progress entry.
     */
    public static class PlayerProgress {
        public boolean hasChronosEye;
        public Set<UUID> stabilizedPortals;
        public Set<String> defeatedBosses;

        public PlayerProgress() {
            this.hasChronosEye = false;
            this.stabilizedPortals = new HashSet<>();
            this.defeatedBosses = new HashSet<>();
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
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(PlayerProgressData::new, PlayerProgressData::load, null),
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

        for (String key : playersTag.getAllKeys()) {
            UUID playerId = UUID.fromString(key);
            CompoundTag playerTag = playersTag.getCompound(key);

            PlayerProgress progress = new PlayerProgress();
            progress.hasChronosEye = playerTag.getBoolean("has_chronos_eye");

            ListTag portalList = playerTag.getList("stabilized_portals", Tag.TAG_STRING);
            for (int i = 0; i < portalList.size(); i++) {
                progress.stabilizedPortals.add(UUID.fromString(portalList.getString(i)));
            }

            ListTag bossList = playerTag.getList("defeated_bosses", Tag.TAG_STRING);
            for (int i = 0; i < bossList.size(); i++) {
                progress.defeatedBosses.add(bossList.getString(i));
            }

            data.playerData.put(playerId, progress);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
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

            playersTag.put(entry.getKey().toString(), playerTag);
        }

        tag.put("players", playersTag);
        return tag;
    }

    // TODO: Add player progress management methods in future phases:
    // - getProgress(UUID playerId)
    // - setChronosEye(UUID playerId, boolean hasEye)
    // - addStabilizedPortal(UUID playerId, UUID portalId)
    // - addDefeatedBoss(UUID playerId, String bossName)
}
