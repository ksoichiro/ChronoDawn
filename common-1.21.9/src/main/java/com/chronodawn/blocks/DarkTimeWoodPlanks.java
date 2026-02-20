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
 * Dark Time Wood Planks block.
 *
 * A variant of Time Wood Planks with a darker appearance.
 * Crafted from Dark Time Wood Logs.
 *
 * Properties:
 * - Hardness: 2.0 (same as Oak Planks)
 * - Blast Resistance: 3.0
 * - Flammable: Yes
 * - Darker color than regular Time Wood Planks
 *
 * Reference: T088t [US1] Design Time Wood color variants
 */
public class DarkTimeWoodPlanks extends Block {
    public DarkTimeWoodPlanks(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Dark Time Wood Planks.
     *
     * @return Block properties with planks-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PODZOL) // Darker brown color
                .strength(2.0f, 3.0f)
                .sound(SoundType.WOOD)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_planks")));
    }
}
