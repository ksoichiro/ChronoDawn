package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.blocks.BossRoomBoundaryMarkerBlockEntity;
import com.chronodawn.blocks.BossRoomDoorBlockEntity;
import com.chronodawn.blocks.ClockTowerTeleporterBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

/**
 * Architectury Registry wrapper for custom block entities.
 */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ChronoDawn.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    /**
     * Clock Tower Teleporter BlockEntity - Stores target teleportation position.
     */
    public static final RegistrySupplier<BlockEntityType<ClockTowerTeleporterBlockEntity>> CLOCK_TOWER_TELEPORTER =
        BLOCK_ENTITIES.register("clock_tower_teleporter", () ->
            // 1.21.2: BlockEntityType.Builder removed, use constructor with Set<Block> directly
            new BlockEntityType<>(
                ClockTowerTeleporterBlockEntity::new,
                Set.<Block>of(ModBlocks.CLOCK_TOWER_TELEPORTER.get())
            )
        );

    /**
     * Boss Room Door BlockEntity - Stores door type ("entrance" or "boss_room").
     */
    public static final RegistrySupplier<BlockEntityType<BossRoomDoorBlockEntity>> BOSS_ROOM_DOOR =
        BLOCK_ENTITIES.register("boss_room_door", () ->
            // 1.21.2: BlockEntityType.Builder removed, use constructor with Set<Block> directly
            new BlockEntityType<>(
                BossRoomDoorBlockEntity::new,
                Set.<Block>of(ModBlocks.BOSS_ROOM_DOOR.get())
            )
        );

    /**
     * Boss Room Boundary Marker BlockEntity - Stores marker type and replacement block.
     */
    public static final RegistrySupplier<BlockEntityType<BossRoomBoundaryMarkerBlockEntity>> BOSS_ROOM_BOUNDARY_MARKER =
        BLOCK_ENTITIES.register("boss_room_boundary_marker", () ->
            // 1.21.2: BlockEntityType.Builder removed, use constructor with Set<Block> directly
            new BlockEntityType<>(
                BossRoomBoundaryMarkerBlockEntity::new,
                Set.<Block>of(ModBlocks.BOSS_ROOM_BOUNDARY_MARKER.get())
            )
        );

    /**
     * Initialize block entity registry.
     */
    public static void register() {
        BLOCK_ENTITIES.register();
        ChronoDawn.LOGGER.debug("Registered ModBlockEntities");
    }
}
