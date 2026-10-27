package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.blocks.BossRoomBoundaryMarkerBlockEntity;
import com.chronodawn.blocks.BossRoomDoorBlockEntity;
import com.chronodawn.blocks.ClockTowerTeleporterBlockEntity;
import com.chronodawn.blocks.MemorialPlateBlockEntity;
import com.chronodawn.blocks.TimeCairnDialBlockEntity;
import com.chronodawn.blocks.TimeWellStoneBlockEntity;
import com.chronodawn.blocks.WatchmakerChestBlockEntity;
import com.chronodawn.blocks.SundialGnomonBlockEntity;
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
     * Sundial Gnomon BlockEntity - stores per-player cooldowns.
     */
    public static final RegistrySupplier<BlockEntityType<SundialGnomonBlockEntity>> SUNDIAL_GNOMON =
        BLOCK_ENTITIES.register(ModBlockEntityId.SUNDIAL_GNOMON.id(), () ->
            BlockEntityType.Builder.of(
                SundialGnomonBlockEntity::new,
                ModBlocks.SUNDIAL_GNOMON.get()
            ).build(null)
        );

    /**
     * Memorial Plate BlockEntity - stores per-player cooldowns.
     */
    public static final RegistrySupplier<BlockEntityType<MemorialPlateBlockEntity>> MEMORIAL_PLATE =
        BLOCK_ENTITIES.register(ModBlockEntityId.MEMORIAL_PLATE.id(), () ->
            BlockEntityType.Builder.of(
                MemorialPlateBlockEntity::new,
                ModBlocks.MEMORIAL_PLATE.get()
            ).build(null)
        );

    /**
     * Time Well Stone BlockEntity - stores per-player cooldowns and the items waiting in the well.
     */
    public static final RegistrySupplier<BlockEntityType<TimeWellStoneBlockEntity>> TIME_WELL_STONE =
        BLOCK_ENTITIES.register(ModBlockEntityId.TIME_WELL_STONE.id(), () ->
            BlockEntityType.Builder.of(
                TimeWellStoneBlockEntity::new,
                ModBlocks.TIME_WELL_STONE.get()
            ).build(null)
        );

    /**
     * Watchmaker's Chest BlockEntity - stores per-player slots and cooldowns.
     */
    public static final RegistrySupplier<BlockEntityType<WatchmakerChestBlockEntity>> WATCHMAKER_CHEST =
        BLOCK_ENTITIES.register(ModBlockEntityId.WATCHMAKER_CHEST.id(), () ->
            BlockEntityType.Builder.of(
                WatchmakerChestBlockEntity::new,
                ModBlocks.WATCHMAKER_CHEST.get()
            ).build(null)
        );

    /**
     * Time Cairn Dial BlockEntity - stores per-player cooldowns.
     */
    public static final RegistrySupplier<BlockEntityType<TimeCairnDialBlockEntity>> TIME_CAIRN_DIAL =
        BLOCK_ENTITIES.register(ModBlockEntityId.TIME_CAIRN_DIAL.id(), () ->
            BlockEntityType.Builder.of(
                TimeCairnDialBlockEntity::new,
                ModBlocks.TIME_CAIRN_DIAL.get()
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
