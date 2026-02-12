package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Global state for ChronoDawn dimension mechanics.
 *
 * This is the 1.21.5-specific version with single-parameter load method
 * and updated NBT API (getBooleanOr instead of getBoolean).
 *
 * This saved data tracks world-level state that affects portal behavior:
 * - hasEnteredChronoDawn: True if any player has entered ChronoDawn
 * - isPortalStabilized: True if Portal Stabilizer has been used
 *
 * Portal Ignition Rules:
 * - If hasEnteredChronoDawn && !isPortalStabilized:
 *   - Time Hourglass cannot ignite portals in ChronoDawn
 *   - Only Portal Stabilizer can enable portal creation
 * - If !hasEnteredChronoDawn || isPortalStabilized:
 *   - Time Hourglass works normally
 */
public class ChronoDawnGlobalState extends CompatSavedData {
    private static final String DATA_NAME = "chronodawn_global_state";

    private boolean hasEnteredChronoDawn = false;
    private boolean isPortalStabilized = false;
    private boolean isTyrantDefeated = false;

    public ChronoDawnGlobalState() {
        super();
    }

    /**
     * Get or create the global state for a server.
     *
     * @param server Minecraft server
     * @return Global state instance
     */
    public static ChronoDawnGlobalState get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return new ChronoDawnGlobalState();
        }

        DimensionDataStorage storage = overworld.getDataStorage();
        return CompatSavedData.computeIfAbsent(
            storage,
            ChronoDawnGlobalState::new,
            ChronoDawnGlobalState::load,
            DATA_NAME
        );
    }

    /**
     * Load global state from NBT.
     * In 1.21.5, the load method no longer needs HolderLookup.Provider.
     *
     * @param tag NBT tag
     * @return Loaded state
     */
    public static ChronoDawnGlobalState load(CompoundTag tag) {
        ChronoDawnGlobalState state = new ChronoDawnGlobalState();
        state.loadData(tag);
        ChronoDawn.LOGGER.debug("Loaded ChronoDawnGlobalState: hasEntered={}, isStabilized={}, tyrantDefeated={}",
            state.hasEnteredChronoDawn, state.isPortalStabilized, state.isTyrantDefeated);
        return state;
    }

    @Override
    public CompoundTag saveData(CompoundTag tag) {
        tag.putBoolean("HasEnteredChronoDawn", hasEnteredChronoDawn);
        tag.putBoolean("IsPortalStabilized", isPortalStabilized);
        tag.putBoolean("IsTyrantDefeated", isTyrantDefeated);
        return tag;
    }

    @Override
    public void loadData(CompoundTag tag) {
        hasEnteredChronoDawn = tag.getBooleanOr("HasEnteredChronoDawn", false);
        isPortalStabilized = tag.getBooleanOr("IsPortalStabilized", false);
        isTyrantDefeated = tag.getBooleanOr("IsTyrantDefeated", false);
    }

    /**
     * Mark that a player has entered ChronoDawn.
     * This triggers the portal instability state.
     */
    public void markChronoDawnEntered() {
        if (!hasEnteredChronoDawn) {
            hasEnteredChronoDawn = true;
            isPortalStabilized = false;
            setDirty();
            ChronoDawn.LOGGER.debug("ChronoDawn entered for the first time - portals are now unstable");
        }
    }

    /**
     * Mark that Portal Stabilizer has been used.
     * This allows portal creation to work normally again.
     */
    public void markPortalStabilized() {
        isPortalStabilized = true;
        setDirty();
        ChronoDawn.LOGGER.debug("Portal stabilized - portals can now be created normally");
    }

    /**
     * Check if portals are currently unstable in ChronoDawn.
     *
     * @return true if Time Hourglass should be blocked in ChronoDawn
     */
    public boolean arePortalsUnstable() {
        return hasEnteredChronoDawn && !isPortalStabilized;
    }

    /**
     * Get whether any player has entered ChronoDawn.
     *
     * @return true if ChronoDawn has been entered
     */
    public boolean hasEnteredChronoDawn() {
        return hasEnteredChronoDawn;
    }

    /**
     * Get whether portals have been stabilized.
     *
     * @return true if Portal Stabilizer has been used
     */
    public boolean isPortalStabilized() {
        return isPortalStabilized;
    }

    /**
     * Mark that Time Tyrant has been defeated.
     * This permanently stabilizes the ChronoDawn dimension.
     */
    public void markTyrantDefeated() {
        isTyrantDefeated = true;
        setDirty();
        ChronoDawn.LOGGER.debug("Time Tyrant defeated - dimension permanently stabilized");
    }

    /**
     * Get whether Time Tyrant has been defeated.
     *
     * @return true if Time Tyrant has been defeated
     */
    public boolean isTyrantDefeated() {
        return isTyrantDefeated;
    }
}
