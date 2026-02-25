package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.compat.CompatSavedData;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalState;
import com.chronodawn.core.portal.PortalStateMachine;
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
 * - Portal state (INACTIVE, ACTIVATED, DEACTIVATED, STABILIZED)
 * - Dimension association
 *
 * Architecture:
 * - This class acts as a bridge between Minecraft's SavedData system and PortalRegistry
 * - Portal data is stored in PortalRegistry (in-memory singleton)
 * - This class saves/loads that data to/from disk via NBT
 *
 * Reference: data-model.md (Data Persistence - Portal Registry)
 */
public class PortalRegistryData extends ChronoDawnWorldData {
    private static final String DATA_NAME = ChronoDawn.MOD_ID + "_portal_registry";

    /**
     * Get or create portal registry data for the given level.
     *
     * @param level ServerLevel to get data from
     * @return Portal registry data instance
     */
    public static PortalRegistryData get(ServerLevel level) {
        return CompatSavedData.computeIfAbsent(
            level.getDataStorage(),
            PortalRegistryData::new,
            PortalRegistryData::load,
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
        data.loadData(tag);
        return data;
    }

    @Override
    public CompoundTag saveData(CompoundTag tag) {
        // Delegate to PortalRegistry for actual data serialization
        PortalRegistry.getInstance().saveToNBT(tag);
        return tag;
    }

    @Override
    public void loadData(CompoundTag tag) {
        // Delegate to PortalRegistry for actual data deserialization
        PortalRegistry.getInstance().loadFromNBT(tag);
    }

    /**
     * Save portal registry data to disk.
     * This marks the data as dirty and triggers a save on next world save.
     */
    public void savePortalRegistry() {
        this.setDirty();
    }
}
