package com.chronodawn.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class for BlockEntity that abstracts version differences (Minecraft 1.21.6).
 *
 * In 1.21.6, saveAdditional/loadAdditional use ValueOutput/ValueInput instead of CompoundTag.
 * This class delegates to version-independent saveData() and loadData() methods.
 *
 * Subclasses should implement BlockEntityDataHandler instead of overriding saveAdditional() directly.
 */
public abstract class CompatBlockEntity extends BlockEntity implements BlockEntityDataHandler {
    public CompatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        saveData(output);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        loadData(input);
    }

    @Override
    public abstract void saveData(ValueOutput output);

    @Override
    public abstract void loadData(ValueInput input);
}
