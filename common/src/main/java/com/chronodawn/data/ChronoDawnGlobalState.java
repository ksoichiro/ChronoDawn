package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Global state for ChronoDawn dimension mechanics.
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
 *
 * This ensures that once a player enters ChronoDawn, they cannot create
 * new portals with Time Hourglass until they use Portal Stabilizer.
 */
public class ChronoDawnGlobalState extends SavedData {
    private static final String DATA_NAME = "chronodawn_global_state";

    private boolean hasEnteredChronoDawn = false;
    private boolean isPortalStabilized = false;
    private boolean isTyrantDefeated = false; // Time Tyrant defeat status (T140)

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
        return storage.computeIfAbsent(
            new SavedData.Factory<>(
                ChronoDawnGlobalState::new,
                ChronoDawnGlobalState::load,
                null
            ),
            DATA_NAME
        );
    }

    /**
     * Load global state from NBT.
     *
     * @param tag NBT tag
     * @param provider Holder lookup provider
     * @return Loaded state
     */
    public static ChronoDawnGlobalState load(CompoundTag tag, HolderLookup.Provider provider) {
        ChronoDawnGlobalState state = new ChronoDawnGlobalState();
        state.hasEnteredChronoDawn = tag.getBoolean("HasEnteredChronoDawn");
        state.isPortalStabilized = tag.getBoolean("IsPortalStabilized");
        state.isTyrantDefeated = tag.getBoolean("IsTyrantDefeated");
        ChronoDawn.LOGGER.info("Loaded ChronoDawnGlobalState: hasEntered={}, isStabilized={}, tyrantDefeated={}",
            state.hasEnteredChronoDawn, state.isPortalStabilized, state.isTyrantDefeated);
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("HasEnteredChronoDawn", hasEnteredChronoDawn);
        tag.putBoolean("IsPortalStabilized", isPortalStabilized);
        tag.putBoolean("IsTyrantDefeated", isTyrantDefeated);
        return tag;
    }

    /**
     * Mark that a player has entered ChronoDawn.
     * This triggers the portal instability state.
     */
    public void markChronoDawnEntered() {
        if (!hasEnteredChronoDawn) {
            hasEnteredChronoDawn = true;
            isPortalStabilized = false; // Reset stabilization on first entry
            setDirty();
            ChronoDawn.LOGGER.info("ChronoDawn entered for the first time - portals are now unstable");
        }
    }

    /**
     * Mark that Portal Stabilizer has been used.
     * This allows portal creation to work normally again.
     */
    public void markPortalStabilized() {
        isPortalStabilized = true;
        setDirty();
        ChronoDawn.LOGGER.info("Portal stabilized - portals can now be created normally");
    }

    /**
     * Check if portals are currently unstable in ChronoDawn.
     * Portals are unstable if player has entered ChronoDawn but hasn't stabilized portals yet.
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
     *
     * Task: T140 [US3] Implement dimension stabilization on defeat
     */
    public void markTyrantDefeated() {
        isTyrantDefeated = true;
        setDirty();
        ChronoDawn.LOGGER.info("Time Tyrant defeated - dimension permanently stabilized");
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
