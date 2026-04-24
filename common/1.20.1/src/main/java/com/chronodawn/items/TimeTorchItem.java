package com.chronodawn.items;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Custom BlockItem for Time Torches that handles wall placement.
 *
 * This item automatically places wall torches when used on vertical surfaces,
 * and floor torches when used on horizontal surfaces or ceilings.
 */
public class TimeTorchItem extends BlockItem {
    private final RegistrySupplier<Block> wallBlock;

    public TimeTorchItem(Block floorBlock, RegistrySupplier<Block> wallBlock, Item.Properties properties) {
        super(floorBlock, properties);
        this.wallBlock = wallBlock;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();

        if (clickedFace.getAxis().isHorizontal()) {
            BlockState wallState = wallBlock.get().defaultBlockState()
                .setValue(WallTorchBlock.FACING, clickedFace);
            if (wallState.canSurvive(context.getLevel(), context.getClickedPos())) {
                return wallState;
            }
        }

        return super.getPlacementState(context);
    }
}
