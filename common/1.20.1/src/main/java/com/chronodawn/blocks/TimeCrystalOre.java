package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Time Crystal Ore block.
 *
 * A rare custom ore block found in the ChronoDawn dimension.
 * Drops Time Crystal item when mined.
 *
 * Properties:
 * - Hardness: 3.5 (slightly harder than Clockstone Ore)
 * - Blast Resistance: 3.5
 * - Requires correct tool (pickaxe) for drops
 * - Fortune enchantment compatible
 * - Spawns at Y: 0-48, vein size 3-5
 *
 * Reference: tasks.md (T210)
 */
public class TimeCrystalOre extends Block {
    public TimeCrystalOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Crystal Ore.
     *
     * @return Block properties with appropriate settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.5f, 3.5f) // hardness, blast resistance (slightly harder than Clockstone Ore)
                .requiresCorrectToolForDrops() // Requires pickaxe
                .sound(SoundType.STONE);
    }
}
