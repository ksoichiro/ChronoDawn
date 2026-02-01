package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Reversing Time Sandstone block.
 *
 * A special block found in the ChronoDawn dimension that automatically restores itself
 * after being destroyed.
 *
 * Properties:
 * - Restoration time: 3 seconds after destruction
 * - No drops when broken (until restored)
 * - Cannot be moved by pistons
 * - If another block is placed before restoration, it will be destroyed and replaced
 *
 * The restoration logic is implemented in BlockEventHandler.java.
 *
 * Reference: data-model.md (Blocks → Reversing Time Sandstone)
 */
public class ReversingTimeSandstone extends Block {
    public ReversingTimeSandstone(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Reversing Time Sandstone.
     *
     * @return Block properties with appropriate settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.8f, 0.8f) // hardness, blast resistance (similar to sandstone)
                .sound(SoundType.STONE)
                .pushReaction(PushReaction.BLOCK) // Cannot be moved by pistons
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "reversing_time_sandstone")));
    }

}
