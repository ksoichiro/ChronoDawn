package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * World saved data for portal registry.
 *
 * This class manages the persistent state of all portals in the world:
 * - Portal ID and location
 * - Portal state (unactivated, activated, stopped, stabilized)
 * - Linked portal IDs (for round-trip travel)
 *
 * Reference: data-model.md (Data Persistence - Portal Registry)
 */
public class PortalRegistryData extends ChronoDawnWorldData {
    private static final String DATA_NAME = ChronoDawn.MOD_ID + "_portal_registry";

    /**
     * Portal state enum.
     */
    public enum PortalState {
        UNACTIVATED,    // Frame exists but not activated
        ACTIVATED,      // Activated with Time Hourglass
        STOPPED,        // Automatically stopped after first use
        STABILIZED      // Stabilized with Portal Stabilizer (permanent)
    }

    /**
     * Portal entry data structure.
     */
    public static class PortalEntry {
        public UUID portalId;
        public ResourceKey<Level> dimension;
        public BlockPos position;
        public PortalState state;
        public UUID linkedPortalId; // null if no link

        public PortalEntry(UUID portalId, ResourceKey<Level> dimension, BlockPos position, PortalState state) {
            this.portalId = portalId;
            this.dimension = dimension;
            this.position = position;
            this.state = state;
            this.linkedPortalId = null;
        }
    }

    private final Map<UUID, PortalEntry> portals = new ConcurrentHashMap<>();

    /**
     * Get or create portal registry data for the given level.
     *
     * @param level ServerLevel to get data from
     * @return Portal registry data instance
     */
    public static PortalRegistryData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(PortalRegistryData::new, PortalRegistryData::load, null),
            DATA_NAME
        );
    }

    /**
     * Load portal registry data from NBT.
     *
     * @param tag CompoundTag to read from
     * @param registries Registry access for deserialization
     * @return Loaded portal registry data instance
     */
    private static PortalRegistryData load(CompoundTag tag, HolderLookup.Provider registries) {
        PortalRegistryData data = new PortalRegistryData();
        ListTag portalList = tag.getList("portals", Tag.TAG_COMPOUND);

        for (int i = 0; i < portalList.size(); i++) {
            CompoundTag portalTag = portalList.getCompound(i);
            UUID portalId = portalTag.getUUID("portal_id");
            // TODO: Load dimension, position, state, linkedPortalId from portalTag
            // This will be fully implemented in future phases when portal system is added
        }

        return data;
    }

    @Override
    public CompoundTag saveData(CompoundTag tag) {
        ListTag portalList = new ListTag();

        for (PortalEntry entry : portals.values()) {
            CompoundTag portalTag = new CompoundTag();
            portalTag.putUUID("portal_id", entry.portalId);
            // TODO: Save dimension, position, state, linkedPortalId to portalTag
            // This will be fully implemented in future phases when portal system is added
            portalList.add(portalTag);
        }

        tag.put("portals", portalList);
        return tag;
    }

    @Override
    public void loadData(CompoundTag tag) {
        ListTag portalList = tag.getList("portals", Tag.TAG_COMPOUND);

        for (int i = 0; i < portalList.size(); i++) {
            CompoundTag portalTag = portalList.getCompound(i);
            UUID portalId = portalTag.getUUID("portal_id");
            // TODO: Load dimension, position, state, linkedPortalId from portalTag
            // This will be fully implemented in future phases when portal system is added
        }
    }

    // TODO: Add portal management methods in future phases:
    // - registerPortal(UUID id, ResourceKey<Level> dimension, BlockPos pos)
    // - setPortalState(UUID id, PortalState state)
    // - linkPortals(UUID portal1, UUID portal2)
    // - getPortal(UUID id)
    // - removePortal(UUID id)
}
