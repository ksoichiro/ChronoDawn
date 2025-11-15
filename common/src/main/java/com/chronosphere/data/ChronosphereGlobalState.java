package com.chronosphere.data;

import com.chronosphere.Chronosphere;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Global state for Chronosphere dimension mechanics.
 *
 * This saved data tracks world-level state that affects portal behavior:
 * - hasEnteredChronosphere: True if any player has entered Chronosphere
 * - isPortalStabilized: True if Portal Stabilizer has been used
 *
 * Portal Ignition Rules:
 * - If hasEnteredChronosphere && !isPortalStabilized:
 *   - Time Hourglass cannot ignite portals in Chronosphere
 *   - Only Portal Stabilizer can enable portal creation
 * - If !hasEnteredChronosphere || isPortalStabilized:
 *   - Time Hourglass works normally
 *
 * This ensures that once a player enters Chronosphere, they cannot create
 * new portals with Time Hourglass until they use Portal Stabilizer.
 */
public class ChronosphereGlobalState extends SavedData {
    private static final String DATA_NAME = "chronosphere_global_state";

    private boolean hasEnteredChronosphere = false;
    private boolean isPortalStabilized = false;
    private boolean isTyrantDefeated = false; // Time Tyrant defeat status (T140)

    public ChronosphereGlobalState() {
        super();
    }

    /**
     * Get or create the global state for a server.
     *
     * @param server Minecraft server
     * @return Global state instance
     */
    public static ChronosphereGlobalState get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return new ChronosphereGlobalState();
        }

        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(
            new SavedData.Factory<>(
                ChronosphereGlobalState::new,
                ChronosphereGlobalState::load,
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
    public static ChronosphereGlobalState load(CompoundTag tag, HolderLookup.Provider provider) {
        ChronosphereGlobalState state = new ChronosphereGlobalState();
        state.hasEnteredChronosphere = tag.getBoolean("HasEnteredChronosphere");
        state.isPortalStabilized = tag.getBoolean("IsPortalStabilized");
        state.isTyrantDefeated = tag.getBoolean("IsTyrantDefeated");
        Chronosphere.LOGGER.info("Loaded ChronosphereGlobalState: hasEntered={}, isStabilized={}, tyrantDefeated={}",
            state.hasEnteredChronosphere, state.isPortalStabilized, state.isTyrantDefeated);
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("HasEnteredChronosphere", hasEnteredChronosphere);
        tag.putBoolean("IsPortalStabilized", isPortalStabilized);
        tag.putBoolean("IsTyrantDefeated", isTyrantDefeated);
        return tag;
    }

    /**
     * Mark that a player has entered Chronosphere.
     * This triggers the portal instability state.
     */
    public void markChronosphereEntered() {
        if (!hasEnteredChronosphere) {
            hasEnteredChronosphere = true;
            isPortalStabilized = false; // Reset stabilization on first entry
            setDirty();
            Chronosphere.LOGGER.info("Chronosphere entered for the first time - portals are now unstable");
        }
    }

    /**
     * Mark that Portal Stabilizer has been used.
     * This allows portal creation to work normally again.
     */
    public void markPortalStabilized() {
        isPortalStabilized = true;
        setDirty();
        Chronosphere.LOGGER.info("Portal stabilized - portals can now be created normally");
    }

    /**
     * Check if portals are currently unstable in Chronosphere.
     * Portals are unstable if player has entered Chronosphere but hasn't stabilized portals yet.
     *
     * @return true if Time Hourglass should be blocked in Chronosphere
     */
    public boolean arePortalsUnstable() {
        return hasEnteredChronosphere && !isPortalStabilized;
    }

    /**
     * Get whether any player has entered Chronosphere.
     *
     * @return true if Chronosphere has been entered
     */
    public boolean hasEnteredChronosphere() {
        return hasEnteredChronosphere;
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
     * This permanently stabilizes the Chronosphere dimension.
     *
     * Task: T140 [US3] Implement dimension stabilization on defeat
     */
    public void markTyrantDefeated() {
        isTyrantDefeated = true;
        setDirty();
        Chronosphere.LOGGER.info("Time Tyrant defeated - dimension permanently stabilized");
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
