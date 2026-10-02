package com.chronodawn.core.portal;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Portal Registry - Manages all ChronoDawn portals.
 *
 * This registry keeps track of all portals across all dimensions, allowing:
 * - Multiple portals per dimension
 * - Portal lookup by ID or position
 * - Portal state persistence
 * - Portal pairing (for bidirectional travel)
 *
 * Portal Pairing:
 * - When a portal is activated in Overworld, it creates a paired portal in ChronoDawn
 * - When stabilized, both portals become bidirectional
 * - Each portal pair shares the same UUID for easy lookup
 *
 * Data Persistence:
 * - Portal data is saved to world save data
 * - State persists across server restarts
 * - Portal positions and states are preserved
 *
 * Performance Optimization (T179):
 * - Uses unmodifiable Sets for dimension portal lookups to avoid defensive copying
 * - ConcurrentHashMap lookups provide O(1) performance for portal access by ID or position
 *
 * Thread Safety (T429):
 * - Uses ConcurrentHashMap for all maps to prevent race conditions in multiplayer
 * - Uses ConcurrentHashMap.newKeySet() for thread-safe Sets
 *
 * Reference: data-model.md (Portal System → Portal Registry)
 * Task: T047 [US1] Implement portal registry
 * Task: T179 [Performance] Implement portal registry caching
 * Task: T429 [Thread Safety] Fix non-thread-safe collection usage
 */
public class PortalRegistry {
    private static final PortalRegistry INSTANCE = new PortalRegistry();

    /** Saved data format. 2 added the version tag and the per-portal legacy counterpart flag. */
    private static final int DATA_VERSION = 2;

    private final Map<UUID, PortalStateMachine> portals;
    private final Map<ResourceKey<Level>, Set<UUID>> portalsByDimension;
    private final Map<ResourceKey<Level>, Map<BlockPos, UUID>> portalsByPosition;

    // T179: Cache unmodifiable views of dimension portal sets for performance
    private final Map<ResourceKey<Level>, Set<UUID>> unmodifiableDimensionPortalCache;

    // Reference to SavedData for automatic persistence
    private com.chronodawn.data.PortalRegistryData savedData;

    private PortalRegistry() {
        // T429: Use ConcurrentHashMap for thread-safe access in multiplayer
        this.portals = new ConcurrentHashMap<>();
        this.portalsByDimension = new ConcurrentHashMap<>();
        this.portalsByPosition = new ConcurrentHashMap<>();
        this.unmodifiableDimensionPortalCache = new ConcurrentHashMap<>();
    }

    /**
     * Get the singleton instance.
     *
     * @return Portal registry instance
     */
    public static PortalRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Set the SavedData instance for automatic persistence.
     * This should be called when the world is loaded.
     *
     * @param savedData PortalRegistryData instance
     */
    public void setSavedData(com.chronodawn.data.PortalRegistryData savedData) {
        this.savedData = savedData;
    }

    /**
     * Mark the portal registry as dirty to trigger a save.
     */
    private void markDirty() {
        if (savedData != null) {
            savedData.savePortalRegistry();
        }
    }

    /**
     * Mark the portal registry as dirty when a portal state changes.
     * Public method for PortalStateMachine to call.
     *
     * @param portalId Portal UUID that changed
     */
    public void markDirtyForPortal(UUID portalId) {
        markDirty();
    }

    /**
     * Register a new portal.
     *
     * @param portal Portal state machine
     */
    public void registerPortal(PortalStateMachine portal) {
        UUID portalId = portal.getPortalId();
        ResourceKey<Level> dimension = portal.getSourceDimension();
        BlockPos position = portal.getPosition();

        // Add to main registry
        portals.put(portalId, portal);

        // Add to dimension index
        // T429: Use ConcurrentHashMap.newKeySet() for thread-safe Set
        portalsByDimension
            .computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet())
            .add(portalId);

        // Add to position index
        portalsByPosition
            .computeIfAbsent(dimension, k -> new ConcurrentHashMap<>())
            .put(position, portalId);

        // T179: Invalidate cache for this dimension
        unmodifiableDimensionPortalCache.remove(dimension);

        // Mark data as dirty for persistence
        markDirty();

        ChronoDawn.LOGGER.debug("Registered portal {} at {} in dimension {}",
            portalId, position, dimension.location());
    }

    /**
     * Unregister a portal.
     *
     * @param portalId Portal UUID
     */
    public void unregisterPortal(UUID portalId) {
        PortalStateMachine portal = portals.remove(portalId);
        if (portal == null) {
            return;
        }

        ResourceKey<Level> dimension = portal.getSourceDimension();

        // Remove from dimension index
        Set<UUID> dimensionPortals = portalsByDimension.get(dimension);
        if (dimensionPortals != null) {
            dimensionPortals.remove(portalId);
        }

        // Remove from position index
        Map<BlockPos, UUID> dimensionPortalPositions = portalsByPosition.get(dimension);
        if (dimensionPortalPositions != null) {
            dimensionPortalPositions.remove(portal.getPosition());
        }

        // T179: Invalidate cache for this dimension
        unmodifiableDimensionPortalCache.remove(dimension);

        // Mark data as dirty for persistence
        markDirty();

        ChronoDawn.LOGGER.debug("Unregistered portal {} from dimension {}",
            portalId, dimension.location());
    }

    /**
     * Get a portal by ID.
     *
     * @param portalId Portal UUID
     * @return Portal state machine, or null if not found
     */
    public PortalStateMachine getPortal(UUID portalId) {
        return portals.get(portalId);
    }

    /**
     * Get a portal by dimension and position.
     *
     * @param dimension Dimension key
     * @param position Portal position
     * @return Portal state machine, or null if not found
     */
    public PortalStateMachine getPortalAt(ResourceKey<Level> dimension, BlockPos position) {
        Map<BlockPos, UUID> dimensionPositions = portalsByPosition.get(dimension);
        if (dimensionPositions == null) {
            return null;
        }
        UUID portalId = dimensionPositions.get(position);
        return portalId != null ? portals.get(portalId) : null;
    }

    /**
     * Find a deactivated portal in another dimension whose counterpart in
     * {@code currentDimension} was never registered, because it was saved before registry
     * lookups were scoped by dimension. Lets a player stabilize from the arrival frame in
     * a world that was upgraded from 0.8.0. Portals created by this version are never
     * returned.
     *
     * @param currentDimension Dimension the player is in
     * @param pos Clicked frame position
     * @param radius Half-width of the cube searched around {@code pos}
     * @return The legacy portal, or null if none is near
     */
    public PortalStateMachine findLegacyPortalNear(ResourceKey<Level> currentDimension, BlockPos pos, int radius) {
        for (PortalStateMachine portal : portals.values()) {
            if (!portal.isLegacyCounterpartPending()
                    || portal.getCurrentState() != PortalState.DEACTIVATED
                    || portal.getSourceDimension().equals(currentDimension)) {
                continue;
            }
            BlockPos portalPos = portal.getPosition();
            if (Math.abs(portalPos.getX() - pos.getX()) <= radius
                    && Math.abs(portalPos.getY() - pos.getY()) <= radius
                    && Math.abs(portalPos.getZ() - pos.getZ()) <= radius) {
                return portal;
            }
        }
        return null;
    }

    /**
     * Get all portals in a dimension.
     * T179: Returns cached unmodifiable Set to avoid defensive copying overhead.
     *
     * @param dimension Dimension key
     * @return Unmodifiable set of portal UUIDs
     */
    public Set<UUID> getPortalsInDimension(ResourceKey<Level> dimension) {
        // T179: Use cached unmodifiable view for performance
        return unmodifiableDimensionPortalCache.computeIfAbsent(dimension, dim -> {
            Set<UUID> dimensionPortals = portalsByDimension.get(dim);
            if (dimensionPortals == null || dimensionPortals.isEmpty()) {
                return Collections.emptySet();
            }
            // T429: Create thread-safe copy using ConcurrentHashMap.newKeySet()
            Set<UUID> copy = ConcurrentHashMap.newKeySet();
            copy.addAll(dimensionPortals);
            return Collections.unmodifiableSet(copy);
        });
    }

    /**
     * Get all portals.
     *
     * @return Collection of all portal state machines
     */
    public Collection<PortalStateMachine> getAllPortals() {
        return portals.values();
    }

    /**
     * Clear all portals (for world unload).
     */
    public void clear() {
        portals.clear();
        portalsByDimension.clear();
        portalsByPosition.clear();
        unmodifiableDimensionPortalCache.clear(); // T179: Clear cache
        ChronoDawn.LOGGER.debug("Cleared all portals from registry");
    }

    /**
     * Save portal data to NBT.
     *
     * @param tag NBT tag to save to
     */
    public void saveToNBT(CompoundTag tag) {
        ListTag portalList = new ListTag();

        for (PortalStateMachine portal : portals.values()) {
            CompoundTag portalTag = new CompoundTag();
            portalTag.putUUID("PortalId", portal.getPortalId());
            portalTag.putString("Dimension", portal.getSourceDimension().location().toString());
            portalTag.putLong("Position", portal.getPosition().asLong());
            portalTag.putString("State", portal.getCurrentState().name());
            if (portal.isLegacyCounterpartPending()) {
                portalTag.putBoolean("LegacyCounterpart", true);
            }
            portalList.add(portalTag);
        }

        tag.put("Portals", portalList);
        tag.putInt("Version", DATA_VERSION);
        ChronoDawn.LOGGER.debug("Saved {} portals to NBT", portals.size());
    }

    /**
     * Load portal data from NBT.
     *
     * @param tag NBT tag to load from
     */
    public void loadFromNBT(CompoundTag tag) {
        clear();

        if (!tag.contains("Portals", Tag.TAG_LIST)) {
            return;
        }

        // Data without a version was written by 0.9.x or earlier. Up to 0.8.0 the position
        // index ignored the dimension, so the arrival portal in the other dimension was
        // never registered when its coordinates matched an existing portal.
        boolean unversioned = !tag.contains("Version", Tag.TAG_INT);

        ListTag portalList = tag.getList("Portals", Tag.TAG_COMPOUND);
        for (int i = 0; i < portalList.size(); i++) {
            CompoundTag portalTag = portalList.getCompound(i);

            UUID portalId = portalTag.getUUID("PortalId");
            // Use CompatResourceLocation.parse() for version compatibility
            ResourceLocation dimensionLoc = CompatResourceLocation.parse(portalTag.getString("Dimension"));
            ResourceKey<Level> dimension = ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                dimensionLoc
            );
            BlockPos position = BlockPos.of(portalTag.getLong("Position"));
            PortalState state = PortalState.valueOf(portalTag.getString("State"));

            PortalStateMachine portal = new PortalStateMachine(portalId, dimension, position);
            portal.setState(state);
            portal.setLegacyCounterpartPending(unversioned
                ? state == PortalState.DEACTIVATED
                : portalTag.getBoolean("LegacyCounterpart"));
            registerPortal(portal);
        }

        ChronoDawn.LOGGER.debug("Loaded {} portals from NBT", portals.size());
    }
}
