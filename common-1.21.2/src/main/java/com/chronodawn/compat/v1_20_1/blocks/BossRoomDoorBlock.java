package com.chronodawn.compat.v1_20_1.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.jetbrains.annotations.Nullable;

/**
 * Custom iron door block with BlockEntity support for NBT data storage.
 *
 * This door looks identical to vanilla iron door but can store custom NBT data
 * to differentiate between entrance door and boss room door in Master Clock dungeon.
 *
 * Features:
 * - Identical appearance and behavior to vanilla iron door
 * - BlockEntity stores door type ("entrance" or "boss_room")
 * - Used in Master Clock structure NBT files with custom NBT tags
 *
 * Usage in structure files:
 * - Entrance door: {DoorType: "entrance"} - requires Key to Master Clock
 * - Boss room door: {DoorType: "boss_room"} - requires 3x Ancient Gears
 *
 * Note: MapCodec is 1.21+ only, not required in 1.20.1
 */
public class BossRoomDoorBlock extends DoorBlock implements EntityBlock {
    public BossRoomDoorBlock(Properties properties) {
        super(properties, BlockSetType.IRON);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BossRoomDoorBlockEntity(pos, state);
    }
}
