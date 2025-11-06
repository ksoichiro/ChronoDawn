package com.chronosphere.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity for Clock Tower Teleporter.
 * Stores the target teleportation position for DOWN teleporters.
 */
public class ClockTowerTeleporterBlockEntity extends BlockEntity {
    private BlockPos targetPos = null;

    public ClockTowerTeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(com.chronosphere.registry.ModBlockEntities.CLOCK_TOWER_TELEPORTER.get(), pos, state);
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (targetPos != null) {
            tag.putInt("TargetX", targetPos.getX());
            tag.putInt("TargetY", targetPos.getY());
            tag.putInt("TargetZ", targetPos.getZ());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("TargetX")) {
            targetPos = new BlockPos(
                tag.getInt("TargetX"),
                tag.getInt("TargetY"),
                tag.getInt("TargetZ")
            );
        }
    }
}
