package com.chronosphere.core.portal;

import com.chronosphere.Chronosphere;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Portal Registry - Manages all Chronosphere portals.
 *
 * This registry keeps track of all portals across all dimensions, allowing:
 * - Multiple portals per dimension
 * - Portal lookup by ID or position
 * - Portal state persistence
 * - Portal pairing (for bidirectional travel)
 *
 * Portal Pairing:
 * - When a portal is activated in Overworld, it creates a paired portal in Chronosphere
 * - When stabilized, both portals become bidirectional
 * - Each portal pair shares the same UUID for easy lookup
 *
 * Data Persistence:
 * - Portal data is saved to world save data
 * - State persists across server restarts
 * - Portal positions and states are preserved
 *
 * Reference: data-model.md (Portal System → Portal Registry)
 * Task: T047 [US1] Implement portal registry
 */
public class PortalRegistry {
    private static final PortalRegistry INSTANCE = new PortalRegistry();

    private final Map<UUID, PortalStateMachine> portals;
    private final Map<ResourceKey<Level>, Set<UUID>> portalsByDimension;
    private final Map<BlockPos, UUID> portalsByPosition;

    private PortalRegistry() {
        this.portals = new HashMap<>();
        this.portalsByDimension = new HashMap<>();
        this.portalsByPosition = new HashMap<>();
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
        portalsByDimension
            .computeIfAbsent(dimension, k -> new HashSet<>())
            .add(portalId);

        // Add to position index
        portalsByPosition.put(position, portalId);

        Chronosphere.LOGGER.info("Registered portal {} at {} in dimension {}",
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

        // Remove from dimension index
        Set<UUID> dimensionPortals = portalsByDimension.get(portal.getSourceDimension());
        if (dimensionPortals != null) {
            dimensionPortals.remove(portalId);
        }

        // Remove from position index
        portalsByPosition.remove(portal.getPosition());

        Chronosphere.LOGGER.info("Unregistered portal {} from dimension {}",
            portalId, portal.getSourceDimension().location());
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
     * @return Set of portal UUIDs
     */
    public Set<UUID> getPortalsInDimension(ResourceKey<Level> dimension) {
        return portalsByDimension.getOrDefault(dimension, Collections.emptySet());
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
        Chronosphere.LOGGER.info("Cleared all portals from registry");
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
            portalList.add(portalTag);
        }

        tag.put("Portals", portalList);
        Chronosphere.LOGGER.info("Saved {} portals to NBT", portals.size());
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

        ListTag portalList = tag.getList("Portals", Tag.TAG_COMPOUND);
        for (int i = 0; i < portalList.size(); i++) {
            CompoundTag portalTag = portalList.getCompound(i);

            UUID portalId = portalTag.getUUID("PortalId");
            ResourceLocation dimensionLoc = ResourceLocation.parse(portalTag.getString("Dimension"));
            ResourceKey<Level> dimension = ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                dimensionLoc
            );
            BlockPos position = BlockPos.of(portalTag.getLong("Position"));
            PortalState state = PortalState.valueOf(portalTag.getString("State"));

            PortalStateMachine portal = new PortalStateMachine(portalId, dimension, position);
            portal.setState(state);
            registerPortal(portal);
        }

        Chronosphere.LOGGER.info("Loaded {} portals from NBT", portals.size());
    }
}
