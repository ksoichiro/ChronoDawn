package com.chronosphere.worldgen.spawning;

import com.chronosphere.Chronosphere;
import com.chronosphere.core.dimension.DimensionStabilizer;
import com.chronosphere.entities.bosses.TimeTyrantEntity;
import com.chronosphere.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.HashSet;
import java.util.Set;

/**
 * Time Tyrant Spawner
 *
 * Manages spawning of Time Tyrant entity in Master Clock structure.
 *
 * Spawn Conditions (from master-clock-design.md):
 * - Location: Master Clock boss room (Chronosphere dimension)
 * - Frequency: Spawns in Master Clock structure
 * - Max per world: 1 (only one Time Tyrant per dimension)
 * - Condition: Only spawns if Time Tyrant has not been defeated
 *
 * Implementation Strategy:
 * - Uses server tick event to periodically check for newly generated Master Clock structures
 * - Spawns Time Tyrant at the boss room center
 * - Tracks spawned structures to avoid duplicate spawning
 * - Respects max spawn limit per world
 * - Checks global defeat status before spawning
 *
 * Reference: master-clock-design.md (Boss Room Specifications)
 * Task: Time Tyrant spawn logic (Master Clock boss room)
 */
public class TimeTyrantSpawner {
    private static final ResourceLocation MASTER_CLOCK_ID = ResourceLocation.fromNamespaceAndPath(
        Chronosphere.MOD_ID,
        "master_clock"
    );

    // Track structure positions where we've already attempted to spawn Time Tyrant
    private static final Set<BlockPos> spawnedStructures = new HashSet<>();

    // Track boss room doors that have already spawned Time Tyrant
    private static final Set<BlockPos> spawnedDoors = new HashSet<>();

    // Maximum Time Tyrants per world
    private static final int MAX_TIME_TYRANTS_PER_WORLD = 1;

    // Counter for spawned Time Tyrants in current world
    private static int spawnedTyrantsCount = 0;

    // Check interval (in ticks) - check every 5 seconds
    private static final int CHECK_INTERVAL = 100;
    private static int tickCounter = 0;

    // Track world dimension to reset counters when changing worlds
    private static ResourceLocation lastWorldId = null;

    /**
     * Initialize Time Tyrant spawning system.
     * Register event listeners for structure generation.
     */
    public static void register() {
        // Use server tick event to check for Master Clock structures
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel serverLevel) {
                Chronosphere.LOGGER.info("Time Tyrant Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        Chronosphere.LOGGER.info("Registered TimeTyrantSpawner");
    }

    /**
     * Check for Master Clock structures and spawn Time Tyrant if needed.
     * This should be called periodically (e.g., from a tick event).
     *
     * @param level The ServerLevel to check
     */
    public static void checkAndSpawnTyrant(ServerLevel level) {
        // Reset tracking if we're in a different world
        ResourceLocation currentWorldId = level.dimension().location();
        if (lastWorldId == null || !lastWorldId.equals(currentWorldId)) {
            reset();
            lastWorldId = currentWorldId;
        }

        // Increment tick counter
        tickCounter++;

        // Only check every CHECK_INTERVAL ticks
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Check if we've reached the spawn limit
        if (spawnedTyrantsCount >= MAX_TIME_TYRANTS_PER_WORLD) {
            return;
        }

        // Check if Time Tyrant has already been defeated
        if (DimensionStabilizer.isTyrantDefeated(level.getServer())) {
            return;
        }

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

                // Check if this chunk contains a Master Clock structure
                if (hasMasterClock(level, chunkPos)) {
                    BlockPos structurePos = chunkPos.getWorldPosition();
                    Chronosphere.LOGGER.info("Found Master Clock structure at chunk {} (block pos: {})", chunkPos, structurePos);

                    // Skip if we've already processed this structure
                    if (spawnedStructures.contains(structurePos)) {
                        Chronosphere.LOGGER.debug("Already processed Master Clock at chunk {}", chunkPos);
                        continue;
                    }

                    // Mark this structure as processed
                    spawnedStructures.add(structurePos);

                    // Spawn Time Tyrant at the boss room
                    spawnTimeTyrantAtBossRoom(level, chunkPos);

                    // Check if we've reached the limit
                    if (spawnedTyrantsCount >= MAX_TIME_TYRANTS_PER_WORLD) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Check if a chunk contains a Master Clock structure.
     *
     * @param level The ServerLevel to check
     * @param chunkPos The chunk position to check
     * @return true if the chunk contains a Master Clock
     */
    private static boolean hasMasterClock(ServerLevel level, ChunkPos chunkPos) {
        // Get structure starts for this chunk
        var structureManager = level.structureManager();

        // Check all structures in this chunk
        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            Structure structure = entry.getKey();

            // Get structure's resource location
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(MASTER_CLOCK_ID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Spawn a Time Tyrant at the Master Clock boss room.
     *
     * @param level The ServerLevel to spawn in
     * @param chunkPos The chunk position containing the structure
     */
    private static void spawnTimeTyrantAtBossRoom(ServerLevel level, ChunkPos chunkPos) {
        // Get the structure start
        var structureManager = level.structureManager();
        BlockPos chunkBlockPos = chunkPos.getWorldPosition();

        // Try to get structure at this position
        for (var entry : structureManager.getAllStructuresAt(chunkBlockPos).entrySet()) {
            Structure structure = entry.getKey();

            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(MASTER_CLOCK_ID)) {
                Chronosphere.LOGGER.info("Attempting to spawn Time Tyrant at Master Clock in chunk {}", chunkPos);

                // Find the boss room position by searching for structure blocks
                BlockPos bossRoomSpawnPos = findBossRoom(level, chunkBlockPos);

                if (bossRoomSpawnPos == null) {
                    Chronosphere.LOGGER.warn("Could not find Master Clock boss room at chunk {}", chunkPos);
                    return;
                }

                Chronosphere.LOGGER.info("Calculated spawn position: {}", bossRoomSpawnPos);

                // Check if a Time Tyrant already exists near this position
                if (isTyrantNearby(level, bossRoomSpawnPos)) {
                    Chronosphere.LOGGER.debug("Time Tyrant already exists near {}", bossRoomSpawnPos);
                    return;
                }

                // Find a valid spawn position (air block with solid ground)
                BlockPos spawnPos = findValidSpawnPosition(level, bossRoomSpawnPos);

                // Create and spawn Time Tyrant
                TimeTyrantEntity tyrant = ModEntities.TIME_TYRANT.get().create(level);
                if (tyrant != null) {
                    tyrant.moveTo(
                        spawnPos.getX() + 0.5,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5,
                        0.0f, 0.0f
                    );

                    tyrant.finalizeSpawn(
                        level,
                        level.getCurrentDifficultyAt(spawnPos),
                        MobSpawnType.STRUCTURE,
                        null
                    );

                    level.addFreshEntity(tyrant);

                    // Increment counter
                    spawnedTyrantsCount++;

                    Chronosphere.LOGGER.info(
                        "Time Tyrant spawned at [{}, {}, {}] (Total: {}/{})",
                        spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                        spawnedTyrantsCount, MAX_TIME_TYRANTS_PER_WORLD
                    );
                }

                // IMPORTANT: Only spawn once per structure
                // Exit immediately after processing this structure
                return;
            }
        }
    }

    /**
     * Find the boss room spawn position of the Master Clock.
     * Searches for Boss Room Door block with "boss_room" door type.
     *
     * @param level The ServerLevel to search in
     * @param searchCenter The center position to search around
     * @return The spawn position in the boss room, or null if not found
     */
    private static BlockPos findBossRoom(ServerLevel level, BlockPos searchCenter) {
        // Search for Boss Room Door with "boss_room" door type
        // This is more reliable than searching for structure blocks
        int searchRadius = 100; // Large radius to cover entire structure
        int maxY = 150; // Search from high altitude
        int minY = -60; // Down to deep underground

        Chronosphere.LOGGER.info("Searching for Boss Room Door from Y={} to Y={} around {}", maxY, minY, searchCenter);

        // Search in all directions for Boss Room Door
        for (int y = maxY; y >= minY; y--) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = new BlockPos(searchCenter.getX() + x, y, searchCenter.getZ() + z);
                    var blockState = level.getBlockState(checkPos);

                    // Check if this is a Boss Room Door block
                    if (blockState.getBlock() instanceof com.chronosphere.blocks.BossRoomDoorBlock) {
                        // Get BlockEntity and check if it's a boss room door
                        var blockEntity = level.getBlockEntity(checkPos);
                        if (blockEntity instanceof com.chronosphere.blocks.BossRoomDoorBlockEntity doorEntity) {
                            if (doorEntity.isBossRoomDoor()) {
                                // Found boss room door! Spawn inside the room
                                // Get door's facing direction and spawn 10 blocks inside
                                var facing = blockState.getValue(net.minecraft.world.level.block.DoorBlock.FACING);

                                // Calculate spawn position (10 blocks inside the door)
                                BlockPos spawnPos = checkPos.relative(facing, 10).above(1);

                                Chronosphere.LOGGER.info("Found Boss Room Door at {} facing {}, spawn at {}",
                                    checkPos, facing, spawnPos);
                                return spawnPos;
                            }
                        }
                    }
                }
            }
        }

        // Boss room door not found
        Chronosphere.LOGGER.warn("Could not find Boss Room Door near {}", searchCenter);
        return null;
    }

    /**
     * Check if a Time Tyrant already exists near the given position.
     *
     * @param level The ServerLevel to check
     * @param pos The position to check around
     * @return true if a Time Tyrant is nearby
     */
    private static boolean isTyrantNearby(ServerLevel level, BlockPos pos) {
        double searchRadius = 50.0; // Search within 50 blocks (boss room is large)
        var nearbyTyrants = level.getEntitiesOfClass(
            TimeTyrantEntity.class,
            new net.minecraft.world.phys.AABB(pos).inflate(searchRadius)
        );
        return !nearbyTyrants.isEmpty();
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
     * Spawn Time Tyrant when Boss Room Door is opened.
     * This is called from BossRoomDoorBlock when a player opens the boss room door.
     *
     * @param level The ServerLevel where the door was opened
     * @param doorPos The position of the door
     * @param doorState The BlockState of the door
     */
    public static void spawnOnDoorOpen(ServerLevel level, BlockPos doorPos, BlockState doorState) {
        Chronosphere.LOGGER.info("Boss Room Door opened at {}", doorPos);

        // Check if Time Tyrant has already been defeated
        if (DimensionStabilizer.isTyrantDefeated(level.getServer())) {
            Chronosphere.LOGGER.info("Time Tyrant already defeated - not spawning");
            return;
        }

        // Check if we've already spawned from this door
        if (spawnedDoors.contains(doorPos)) {
            Chronosphere.LOGGER.info("Time Tyrant already spawned from this door - not spawning again");
            return;
        }

        // Check if we've reached the spawn limit
        if (spawnedTyrantsCount >= MAX_TIME_TYRANTS_PER_WORLD) {
            Chronosphere.LOGGER.info("Time Tyrant spawn limit reached ({}/{}) - not spawning",
                spawnedTyrantsCount, MAX_TIME_TYRANTS_PER_WORLD);
            return;
        }

        // Get door's facing direction
        var facing = doorState.getValue(DoorBlock.FACING);

        // Calculate spawn position (15 blocks inside the door, 4 blocks above ground)
        // Boss room has elevated central platform
        BlockPos spawnPos = doorPos.relative(facing, 15).above(4);

        // Find valid spawn position (air with solid ground)
        spawnPos = findValidSpawnPosition(level, spawnPos);

        // Check if a Time Tyrant already exists near this position
        if (isTyrantNearby(level, spawnPos)) {
            Chronosphere.LOGGER.info("Time Tyrant already exists near {} - not spawning", spawnPos);
            return;
        }

        // Create and spawn Time Tyrant
        TimeTyrantEntity tyrant = ModEntities.TIME_TYRANT.get().create(level);
        if (tyrant != null) {
            tyrant.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                0.0f, 0.0f
            );

            tyrant.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.TRIGGERED, // Use TRIGGERED instead of STRUCTURE
                null
            );

            level.addFreshEntity(tyrant);

            // Mark this door as spawned
            spawnedDoors.add(doorPos);

            // Increment counter
            spawnedTyrantsCount++;

            Chronosphere.LOGGER.info(
                "Time Tyrant spawned at [{}, {}, {}] from Boss Room Door (Total: {}/{})",
                spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                spawnedTyrantsCount, MAX_TIME_TYRANTS_PER_WORLD
            );
        } else {
            Chronosphere.LOGGER.error("Failed to create Time Tyrant entity");
        }
    }

    /**
     * Reset spawn tracking (useful for testing or world reset).
     */
    public static void reset() {
        spawnedStructures.clear();
        spawnedDoors.clear();
        spawnedTyrantsCount = 0;
        tickCounter = 0;
        lastWorldId = null;
        Chronosphere.LOGGER.info("Time Tyrant Spawner reset");
    }
}
