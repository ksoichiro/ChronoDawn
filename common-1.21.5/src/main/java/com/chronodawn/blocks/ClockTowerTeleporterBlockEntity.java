package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity for Clock Tower Teleporter (1.21.5 version).
 * Stores the target teleportation position for DOWN teleporters.
 *
 * Note: In 1.21.5, NBT getInt() returns Optional, so we use getIntOr().
 */
public class ClockTowerTeleporterBlockEntity extends CompatBlockEntity {
    private BlockPos targetPos = null;

    public ClockTowerTeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronodawn.registry.ModBlockEntities.CLOCK_TOWER_TELEPORTER.get(), pos, state);
    }

    /**
     * Set the target teleportation position.
     */
    public void setTargetPos(BlockPos pos) {
        this.targetPos = pos;
        setChanged();
    }

    /**
     * Get the target teleportation position.
     */
    public BlockPos getTargetPos() {
        return targetPos;
    }

    @Override
    public void saveData(CompoundTag tag) {
        if (targetPos != null) {
            tag.putInt("TargetX", targetPos.getX());
            tag.putInt("TargetY", targetPos.getY());
            tag.putInt("TargetZ", targetPos.getZ());
        }
    }

    @Override
    public void loadData(CompoundTag tag) {
        if (tag.contains("TargetX")) {
            // In 1.21.5, getInt() returns Optional, so use getIntOr() with default values
            targetPos = new BlockPos(
                tag.getIntOr("TargetX", 0),
                tag.getIntOr("TargetY", 0),
                tag.getIntOr("TargetZ", 0)
            );
        }
    }
}
