package com.chronodawn.compat.v1_20_1;

import com.chronodawn.compat.BlockEntityDataHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class for BlockEntity that abstracts version differences (Minecraft 1.20.1).
 *
 * This class handles the 1.20.1 saveAdditional/load signature (without HolderLookup.Provider)
 * and delegates to version-independent saveData() and loadData() methods.
 *
 * Subclasses should implement BlockEntityDataHandler instead of overriding saveAdditional() directly.
 *
 * Usage Example:
 * <pre>{@code
 * public class MyBlockEntity extends CompatBlockEntity {
 *     public MyBlockEntity(BlockPos pos, BlockState state) {
 *         super(ModBlockEntities.MY_BLOCK_ENTITY.get(), pos, state);
 *     }
 *
 *     @Override
 *     public void saveData(CompoundTag tag) {
 *         tag.putString("DoorType", doorType);
 *     }
 *
 *     @Override
 *     public void loadData(CompoundTag tag) {
 *         if (tag.contains("DoorType")) {
 *             doorType = tag.getString("DoorType");
 *         }
 *     }
 * }
 * }</pre>
 */
public abstract class CompatBlockEntity extends BlockEntity implements BlockEntityDataHandler {
    public CompatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Save BlockEntity data to NBT (Minecraft 1.20.1 signature).
     * This method delegates to version-independent saveData().
     *
     * @param tag CompoundTag to write to
     */
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        // Call version-independent saveData()
        saveData(tag);
    }

    /**
     * Load BlockEntity data from NBT (Minecraft 1.20.1 signature).
     * This method delegates to version-independent loadData().
     *
     * @param tag CompoundTag to read from
     */
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // Call version-independent loadData()
        loadData(tag);
    }

    /**
     * Version-independent save method.
     * Subclasses must implement this.
     *
     * @param tag CompoundTag to write to
     */
    @Override
    public abstract void saveData(CompoundTag tag);

    /**
     * Version-independent load method.
     * Subclasses must implement this.
     *
     * @param tag CompoundTag to read from
     */
    @Override
    public abstract void loadData(CompoundTag tag);
}
