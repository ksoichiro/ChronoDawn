package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.blocks.BossRoomBoundaryMarkerBlockEntity;
import com.chronodawn.blocks.BossRoomDoorBlockEntity;
import com.chronodawn.blocks.ClockTowerTeleporterBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.chronodawn.registry.ModBlockEntityId;

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
        BLOCK_ENTITIES.register(ModBlockEntityId.CLOCK_TOWER_TELEPORTER.id(), () ->
            BlockEntityType.Builder.of(
                ClockTowerTeleporterBlockEntity::new,
                ModBlocks.CLOCK_TOWER_TELEPORTER.get()
            ).build(null)
        );

    /**
     * Boss Room Door BlockEntity - Stores door type ("entrance" or "boss_room").
     */
    public static final RegistrySupplier<BlockEntityType<BossRoomDoorBlockEntity>> BOSS_ROOM_DOOR =
        BLOCK_ENTITIES.register(ModBlockEntityId.BOSS_ROOM_DOOR.id(), () ->
            BlockEntityType.Builder.of(
                BossRoomDoorBlockEntity::new,
                ModBlocks.BOSS_ROOM_DOOR.get()
            ).build(null)
        );

    /**
     * Boss Room Boundary Marker BlockEntity - Stores marker type and replacement block.
     */
    public static final RegistrySupplier<BlockEntityType<BossRoomBoundaryMarkerBlockEntity>> BOSS_ROOM_BOUNDARY_MARKER =
        BLOCK_ENTITIES.register(ModBlockEntityId.BOSS_ROOM_BOUNDARY_MARKER.id(), () ->
            BlockEntityType.Builder.of(
                BossRoomBoundaryMarkerBlockEntity::new,
                ModBlocks.BOSS_ROOM_BOUNDARY_MARKER.get()
            ).build(null)
        );

    /**
     * Initialize block entity registry.
     */
    public static void register() {
        BLOCK_ENTITIES.register();
        ChronoDawn.LOGGER.debug("Registered ModBlockEntities");
    }
}
