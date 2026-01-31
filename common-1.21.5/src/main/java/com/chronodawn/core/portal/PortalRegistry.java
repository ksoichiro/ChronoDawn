package com.chronodawn.core.portal;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Portal Registry - Manages all ChronoDawn portals.
 *
 * This is the 1.21.5-specific version with updated NBT API.
 * - UUID stored as string (putUUID/getUUID removed in 1.21.5)
 * - Uses getStringOr/getLongOr instead of getString/getLong
 * - Uses getListOrEmpty instead of getList with type
 *
 * This registry keeps track of all portals across all dimensions, allowing:
 * - Multiple portals per dimension
 * - Portal lookup by ID or position
 * - Portal state persistence
 * - Portal pairing (for bidirectional travel)
 */
public class PortalRegistry {
    private static final PortalRegistry INSTANCE = new PortalRegistry();

    private final Map<UUID, PortalStateMachine> portals;
    private final Map<ResourceKey<Level>, Set<UUID>> portalsByDimension;
    private final Map<BlockPos, UUID> portalsByPosition;
    private final Map<ResourceKey<Level>, Set<UUID>> unmodifiableDimensionPortalCache;

    private com.chronodawn.data.PortalRegistryData savedData;

    private PortalRegistry() {
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

        portals.put(portalId, portal);

        portalsByDimension
            .computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet())
            .add(portalId);

        portalsByPosition.put(position, portalId);

        unmodifiableDimensionPortalCache.remove(dimension);

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

        Set<UUID> dimensionPortals = portalsByDimension.get(dimension);
        if (dimensionPortals != null) {
            dimensionPortals.remove(portalId);
        }

        portalsByPosition.remove(portal.getPosition());

        unmodifiableDimensionPortalCache.remove(dimension);

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
     * Get a portal by position.
     *
     * @param position Portal position
     * @return Portal state machine, or null if not found
     */
    public PortalStateMachine getPortalAt(BlockPos position) {
        UUID portalId = portalsByPosition.get(position);
        return portalId != null ? portals.get(portalId) : null;
    }

    /**
     * Get all portals in a dimension.
     *
     * @param dimension Dimension key
     * @return Unmodifiable set of portal UUIDs
     */
    public Set<UUID> getPortalsInDimension(ResourceKey<Level> dimension) {
        return unmodifiableDimensionPortalCache.computeIfAbsent(dimension, dim -> {
            Set<UUID> dimensionPortals = portalsByDimension.get(dim);
            if (dimensionPortals == null || dimensionPortals.isEmpty()) {
                return Collections.emptySet();
            }
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
        unmodifiableDimensionPortalCache.clear();
        ChronoDawn.LOGGER.debug("Cleared all portals from registry");
    }

    /**
     * Save portal data to NBT.
     * In 1.21.5, UUID is saved as string since putUUID/getUUID were removed.
     *
     * @param tag NBT tag to save to
     */
    public void saveToNBT(CompoundTag tag) {
        ListTag portalList = new ListTag();

        for (PortalStateMachine portal : portals.values()) {
            CompoundTag portalTag = new CompoundTag();
            portalTag.putString("PortalId", portal.getPortalId().toString());
            portalTag.putString("Dimension", portal.getSourceDimension().location().toString());
            portalTag.putLong("Position", portal.getPosition().asLong());
            portalTag.putString("State", portal.getCurrentState().name());
            portalList.add(portalTag);
        }

        tag.put("Portals", portalList);
        ChronoDawn.LOGGER.debug("Saved {} portals to NBT", portals.size());
    }

    /**
     * Load portal data from NBT.
     * In 1.21.5, UUID is loaded from string and uses getStringOr/getLongOr.
     *
     * @param tag NBT tag to load from
     */
    public void loadFromNBT(CompoundTag tag) {
        clear();

        ListTag portalList = tag.getListOrEmpty("Portals");
        for (int i = 0; i < portalList.size(); i++) {
            CompoundTag portalTag = portalList.getCompound(i).orElse(new CompoundTag());

            String portalIdStr = portalTag.getStringOr("PortalId", "");
            if (portalIdStr.isEmpty()) {
                ChronoDawn.LOGGER.warn("Skipping portal with missing ID at index {}", i);
                continue;
            }

            UUID portalId;
            try {
                portalId = UUID.fromString(portalIdStr);
            } catch (IllegalArgumentException e) {
                ChronoDawn.LOGGER.warn("Skipping portal with invalid UUID: {}", portalIdStr);
                continue;
            }

            String dimensionStr = portalTag.getStringOr("Dimension", "");
            if (dimensionStr.isEmpty()) {
                ChronoDawn.LOGGER.warn("Skipping portal {} with missing dimension", portalId);
                continue;
            }

            ResourceLocation dimensionLoc = CompatResourceLocation.parse(dimensionStr);
            ResourceKey<Level> dimension = ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                dimensionLoc
            );
            BlockPos position = BlockPos.of(portalTag.getLongOr("Position", 0L));

            String stateStr = portalTag.getStringOr("State", "INACTIVE");
            PortalState state;
            try {
                state = PortalState.valueOf(stateStr);
            } catch (IllegalArgumentException e) {
                state = PortalState.INACTIVE;
                ChronoDawn.LOGGER.warn("Invalid portal state '{}' for portal {}, defaulting to INACTIVE",
                    stateStr, portalId);
            }

            PortalStateMachine portal = new PortalStateMachine(portalId, dimension, position);
            portal.setState(state);
            registerPortal(portal);
        }

        ChronoDawn.LOGGER.debug("Loaded {} portals from NBT", portals.size());
    }
}
