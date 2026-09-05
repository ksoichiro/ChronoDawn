package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void saveData(ValueOutput output) {
        if (targetPos != null) {
            output.putInt("TargetX", targetPos.getX());
            output.putInt("TargetY", targetPos.getY());
            output.putInt("TargetZ", targetPos.getZ());
        }
    }

    @Override
    public void loadData(ValueInput input) {
        input.getInt("TargetX").ifPresent(x ->
            targetPos = new BlockPos(
                x,
                input.getIntOr("TargetY", 0),
                input.getIntOr("TargetZ", 0)
            )
        );
    }
}
