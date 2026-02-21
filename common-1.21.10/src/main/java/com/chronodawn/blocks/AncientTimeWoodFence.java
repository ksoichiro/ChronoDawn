package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Ancient Time Wood Fence - Fence variant of Ancient Time Wood Planks.
 *
 * Standard fence block that connects to other fences and blocks.
 *
 * Crafting:
 * - 4x Ancient Time Wood Planks + 2x Stick → 3x Ancient Time Wood Fence
 */
public class AncientTimeWoodFence extends FenceBlock {
    public AncientTimeWoodFence(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Ancient Time Wood Fence.
     *
     * @return Block properties for wooden fence
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 3.0f)
                .sound(SoundType.WOOD)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_fence")));
    }
}
