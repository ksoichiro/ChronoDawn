package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Time Wood Planks block.
 *
 * Crafted from Time Wood Logs, used as a building material.
 * Standard wooden planks block with time-themed appearance.
 *
 * Properties:
 * - Hardness: 2.0 (same as Oak Planks)
 * - Blast Resistance: 3.0 (same as Oak Planks)
 * - Flammable: Yes
 * - Tool Required: None (can be broken by hand)
 *
 * Crafting:
 * - 1x Time Wood Log → 4x Time Wood Planks
 *
 * Visual:
 * - Texture: time_wood_planks.png (time-themed wooden planks)
 */
public class TimeWoodPlanks extends Block {
    public TimeWoodPlanks(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Wood Planks.
     *
     * @return Block properties with planks-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 3.0f) // hardness, blast resistance (same as Oak Planks)
                .sound(SoundType.WOOD)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_planks")));
    }
}
