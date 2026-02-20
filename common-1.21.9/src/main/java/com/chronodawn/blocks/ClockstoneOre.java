package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Clockstone Ore block.
 *
 * A custom ore block found in the Overworld (Ancient Ruins) and ChronoDawn dimension.
 * Drops Clockstone item when mined.
 *
 * Properties:
 * - Hardness: 3.0 (similar to Iron Ore)
 * - Blast Resistance: 3.0
 * - Requires correct tool (pickaxe) for drops
 * - Fortune enchantment compatible
 *
 * Reference: data-model.md (Blocks → Clockstone Ore)
 */
public class ClockstoneOre extends Block {
    public ClockstoneOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone Ore.
     *
     * @return Block properties with appropriate settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                .strength(3.0f, 3.0f) // hardness, blast resistance
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_ore")));
    }
}
