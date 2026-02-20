package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import com.chronodawn.core.portal.PortalRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/**
 * World saved data for portal registry.
 *
 * This is the 1.21.5-specific version with single-parameter load method.
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
     * In 1.21.5, the load method no longer needs HolderLookup.Provider.
     *
     * @param tag CompoundTag to read from
     * @return Loaded portal registry data instance
     */
    private static PortalRegistryData load(CompoundTag tag) {
        PortalRegistryData data = new PortalRegistryData();
        data.loadData(tag);
        return data;
    }

    @Override
    public CompoundTag saveData(CompoundTag tag) {
        PortalRegistry.getInstance().saveToNBT(tag);
        return tag;
    }

    @Override
    public void loadData(CompoundTag tag) {
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
