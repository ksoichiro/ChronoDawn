package com.chronodawn.compat.v1_20_1.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.ChronosWardenEntity;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Chronos Warden Spawner
 *
 * Manages spawning of Chronos Warden entity in Guardian Vault structure.
 *
 * Spawn Conditions:
 * - Location: Guardian Vault structure (ChronoDawn dimension)
 * - Frequency: Spawns in Guardian Vault structure
 * - Max per world: Multiple (one per Guardian Vault structure)
 * - Condition: Spawns when Guardian Vault door is opened or structure is discovered
 *
 * Implementation Strategy:
 * - Uses server tick event to periodically check for newly generated Guardian Vault structures
 * - Spawns Chronos Warden at the boss room center
 * - Tracks spawned structures to avoid duplicate spawning
 * - Spawns on door open event
 *
 * Reference: T234 - Chronos Warden implementation
 * Task: Chronos Warden spawn logic (Guardian Vault structure)
 */
public class ChronosWardenSpawner {
    private static final ResourceLocation GUARDIAN_VAULT_ID = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "guardian_vault"
    );

    // Track structure positions where we've already attempted to spawn Chronos Warden
    private static final Set<BlockPos> spawnedStructures = Collections.synchronizedSet(new HashSet<>());

    // Track boss room doors that have already spawned Chronos Warden
    private static final Set<BlockPos> spawnedDoors = Collections.synchronizedSet(new HashSet<>());

    // Check interval (in ticks) - check every 5 seconds
    private static final int CHECK_INTERVAL = 100;
    // T430: Per-dimension tick counters to prevent cross-dimension interference
    private static final Map<ResourceLocation, Integer> tickCounters = new java.util.HashMap<>();

    // Track world dimension to reset counters when changing worlds
    private static ResourceLocation lastWorldId = null;

    /**
     * Initialize Chronos Warden spawning system.
     * Register event listeners for structure generation.
     */
    public static void register() {
        // Use server tick event to check for Guardian Vault structures
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) level;
                ChronoDawn.LOGGER.info("Chronos Warden Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        ChronoDawn.LOGGER.info("Registered ChronosWardenSpawner");
    }

    /**
     * Check for Guardian Vault structures and spawn Chronos Warden if needed.
     * This should be called periodically (e.g., from a tick event).
     *
     * @param level The ServerLevel to check
     */
    public static void checkAndSpawnWarden(ServerLevel level) {
        // Reset tracking if we're in a different world
        ResourceLocation currentWorldId = level.dimension().location();
        if (lastWorldId == null || !lastWorldId.equals(currentWorldId)) {
            reset();
            lastWorldId = currentWorldId;
        }

        // T430: Use per-dimension tick counter
        tickCounters.putIfAbsent(currentWorldId, 0);
        int tickCounter = tickCounters.get(currentWorldId) + 1;

        // Only check every CHECK_INTERVAL ticks
        if (tickCounter < CHECK_INTERVAL) {
            tickCounters.put(currentWorldId, tickCounter);
            return;
        }
        tickCounters.put(currentWorldId, 0);

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

        // Check chunks around the first player only (to avoid duplicate processing)
        var player = level.players().get(0);
        ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());

        // Check chunks in a 10-chunk radius around player
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);

                // Check if this chunk contains a Guardian Vault structure
                if (hasGuardianVault(level, chunkPos)) {
                    BlockPos structurePos = chunkPos.getWorldPosition();

                    // Skip if we've already processed this structure
                    if (spawnedStructures.contains(structurePos)) {
                        continue;
                    }

                    // Mark this structure as processed
                    spawnedStructures.add(structurePos);

                    ChronoDawn.LOGGER.info("Found Guardian Vault structure at chunk {} (block pos: {})", chunkPos, structurePos);

                    // Spawn Chronos Warden at the boss room
                    spawnChronosWardenAtBossRoom(level, chunkPos);
                }
            }
        }
    }

    /**
     * Check if a chunk contains a Guardian Vault structure.
     *
     * @param level The ServerLevel to check
     * @param chunkPos The chunk position to check
     * @return true if the chunk contains a Guardian Vault
     */
    private static boolean hasGuardianVault(ServerLevel level, ChunkPos chunkPos) {
        // Get structure starts for this chunk
        var structureManager = level.structureManager();

        // Check all structures in this chunk
        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            Structure structure = entry.getKey();

            // Get structure's resource location
            var structureLocation = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(GUARDIAN_VAULT_ID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Spawn a Chronos Warden at the Guardian Vault boss room.
     *
     * @param level The ServerLevel to spawn in
     * @param chunkPos The chunk position containing the structure
     */
    private static void spawnChronosWardenAtBossRoom(ServerLevel level, ChunkPos chunkPos) {
        // Get the structure start
        var structureManager = level.structureManager();
        BlockPos chunkBlockPos = chunkPos.getWorldPosition();

        // Try to get structure at this position
        for (var entry : structureManager.getAllStructuresAt(chunkBlockPos).entrySet()) {
            Structure structure = entry.getKey();

            var structureLocation = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(GUARDIAN_VAULT_ID)) {
                ChronoDawn.LOGGER.info("Attempting to spawn Chronos Warden at Guardian Vault in chunk {}", chunkPos);

                // Find the boss room position by searching for structure blocks
                BlockPos bossRoomSpawnPos = findBossRoom(level, chunkBlockPos);

                if (bossRoomSpawnPos == null) {
                    ChronoDawn.LOGGER.warn("Could not find Guardian Vault boss room at chunk {}", chunkPos);
                    return;
                }

                ChronoDawn.LOGGER.info("Calculated spawn position: {}", bossRoomSpawnPos);

                // Check if a Chronos Warden already exists near this position
                if (isWardenNearby(level, bossRoomSpawnPos)) {
                    ChronoDawn.LOGGER.debug("Chronos Warden already exists near {}", bossRoomSpawnPos);
                    return;
                }

                // Find a valid spawn position (air block with solid ground)
                BlockPos spawnPos = findValidSpawnPosition(level, bossRoomSpawnPos);

                // Create and spawn Chronos Warden
                ChronosWardenEntity warden = ModEntities.CHRONOS_WARDEN.get().create(level);
                if (warden != null) {
                    warden.moveTo(
                        spawnPos.getX() + 0.5,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5,
                        0.0f, 0.0f
                    );

                    warden.finalizeSpawn(
                        level,
                        level.getCurrentDifficultyAt(spawnPos),
                        MobSpawnType.STRUCTURE,
                        null,
                        null
                    );

                    level.addFreshEntity(warden);

                    ChronoDawn.LOGGER.info(
                        "Chronos Warden spawned at [{}, {}, {}]",
                        spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()
                    );
                }

                // IMPORTANT: Only spawn once per structure
                // Exit immediately after processing this structure
                return;
            }
        }
    }

    /**
     * Find the boss room spawn position of the Guardian Vault.
     * Searches for Boss Room Door block with "guardian_vault" door type.
     *
     * @param level The ServerLevel to search in
     * @param searchCenter The center position to search around
     * @return The spawn position in the boss room, or null if not found
     */
    private static BlockPos findBossRoom(ServerLevel level, BlockPos searchCenter) {
        // Search for Boss Room Door with "guardian_vault" door type
        int searchRadius = 100; // Large radius to cover entire structure
        int maxY = 150; // Search from high altitude
        int minY = -60; // Down to deep underground

        ChronoDawn.LOGGER.info("Searching for Guardian Vault Door from Y={} to Y={} around {}", maxY, minY, searchCenter);

        // Search in all directions for Guardian Vault Door
        for (int y = maxY; y >= minY; y--) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = new BlockPos(searchCenter.getX() + x, y, searchCenter.getZ() + z);
                    var blockState = level.getBlockState(checkPos);

                    // Check if this is a Boss Room Door block
                    if (blockState.getBlock() instanceof com.chronodawn.blocks.BossRoomDoorBlock) {
                        // Get BlockEntity and check if it's a guardian vault door
                        var blockEntity = level.getBlockEntity(checkPos);
                        if (blockEntity instanceof com.chronodawn.blocks.BossRoomDoorBlockEntity doorEntity) {
                            if (doorEntity.isGuardianVaultDoor()) {
                                // Found guardian vault door! Spawn inside the room
                                // Get door's facing direction
                                var facing = blockState.getValue(net.minecraft.world.level.block.DoorBlock.FACING);

                                // Calculate spawn position (10 blocks inside the door, 10 blocks to the right)
                                // This places the boss at the center of the room
                                BlockPos spawnPos = checkPos.relative(facing, 10)           // 10 blocks inside
                                                            .relative(facing.getClockWise(), 10)  // 10 blocks to the right
                                                            .above(1);

                                ChronoDawn.LOGGER.info("Found Guardian Vault Door at {} facing {}, spawn at {}",
                                    checkPos, facing, spawnPos);
                                return spawnPos;
                            }
                        }
                    }
                }
            }
        }

        // Boss room door not found
        ChronoDawn.LOGGER.warn("Could not find Guardian Vault Door near {}", searchCenter);
        return null;
    }

    /**
     * Check if a Chronos Warden already exists near the given position.
     *
     * @param level The ServerLevel to check
     * @param pos The position to check around
     * @return true if a Chronos Warden is nearby
     */
    private static boolean isWardenNearby(ServerLevel level, BlockPos pos) {
        double searchRadius = 50.0; // Search within 50 blocks
        var nearbyWardens = level.getEntitiesOfClass(
            ChronosWardenEntity.class,
            new net.minecraft.world.phys.AABB(pos).inflate(searchRadius)
        );
        return !nearbyWardens.isEmpty();
    }

    /**
     * Find a valid spawn position near the given position.
     * Looks for an air block with solid ground beneath.
     *
     * @param level The ServerLevel to check
     * @param startPos The starting position to search from
     * @return A valid spawn position
     */
    private static BlockPos findValidSpawnPosition(ServerLevel level, BlockPos startPos) {
        // Search downward up to 10 blocks
        for (int i = 0; i < 10; i++) {
            BlockPos checkPos = startPos.below(i);

            // Check if this is a valid spawn position (air with solid ground)
            if (level.getBlockState(checkPos).isAir() &&
                !level.getBlockState(checkPos.below()).isAir()) {
                return checkPos;
            }
        }

        // If no valid position found, return original position
        return startPos;
    }

    /**
     * Spawn Chronos Warden when Guardian Vault Door is opened.
     * This is called from BossRoomDoorBlock when a player opens the guardian vault door.
     *
     * @param level The ServerLevel where the door was opened
     * @param doorPos The position of the door
     * @param doorState The BlockState of the door
     */
    public static void spawnOnDoorOpen(ServerLevel level, BlockPos doorPos, BlockState doorState) {
        ChronoDawn.LOGGER.info("Guardian Vault Door opened at {}", doorPos);

        // Check if we've already spawned from this door
        if (spawnedDoors.contains(doorPos)) {
            ChronoDawn.LOGGER.info("Chronos Warden already spawned from this door - not spawning again");
            return;
        }

        // Get door's facing direction
        var facing = doorState.getValue(DoorBlock.FACING);

        // Calculate spawn position (10 blocks inside the door, 10 blocks to the right)
        // This places the boss at the center of the room
        BlockPos spawnPos = doorPos.relative(facing, 10)           // 10 blocks inside
                                   .relative(facing.getClockWise(), 10)  // 10 blocks to the right
                                   .above(1);

        // Find valid spawn position (air with solid ground)
        spawnPos = findValidSpawnPosition(level, spawnPos);

        // Check if a Chronos Warden already exists near this position
        if (isWardenNearby(level, spawnPos)) {
            ChronoDawn.LOGGER.info("Chronos Warden already exists near {} - not spawning", spawnPos);
            return;
        }

        // Note: Boss room protection is now handled by BossRoomProtectionProcessor
        // during structure generation (marker blocks in NBT → BoundingBox calculation)

        // Create and spawn Chronos Warden
        ChronosWardenEntity warden = ModEntities.CHRONOS_WARDEN.get().create(level);
        if (warden != null) {
            warden.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                0.0f, 0.0f
            );

            warden.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.TRIGGERED, // Use TRIGGERED instead of STRUCTURE
                null,
                null
            );

            // Note: Door position no longer needed for protection (handled by marker blocks)
            // Boss room protection uses marker block positions as unique IDs

            level.addFreshEntity(warden);

            // Mark this door as spawned
            spawnedDoors.add(doorPos);

            ChronoDawn.LOGGER.info(
                "Chronos Warden spawned at [{}, {}, {}] from Guardian Vault Door",
                spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()
            );
        } else {
            ChronoDawn.LOGGER.error("Failed to create Chronos Warden entity");
        }
    }

    /**
     * Reset spawn tracking (useful for testing or world reset).
     * T430: Now clears per-dimension tick counters.
     */
    public static void reset() {
        spawnedStructures.clear();
        spawnedDoors.clear();
        tickCounters.clear();
        lastWorldId = null;
        ChronoDawn.LOGGER.info("Chronos Warden Spawner reset");
    }
}
