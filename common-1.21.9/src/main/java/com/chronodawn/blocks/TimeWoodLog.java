package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Time Wood Log block.
 *
 * A custom log block used in Fruit of Time trees found in the ChronoDawn dimension.
 * Features time-themed textures and serves as the trunk of custom trees.
 *
 * Properties:
 * - Hardness: 2.0 (same as Oak Log)
 * - Blast Resistance: 2.0
 * - Flammable: Yes (compatible with fire mechanics)
 * - Strippable: Can be stripped with axe (future enhancement)
 * - Directional: Can be placed in X, Y, Z axis
 *
 * Visual:
 * - Side texture: time_wood_log.png (time-themed bark)
 * - Top texture: time_wood_log_top.png (time-themed rings)
 *
 * Reference: T080a [US1] Create Time Wood Log block
 * Related: FruitOfTimeTreeFeature uses this for tree generation
 */
public class TimeWoodLog extends RotatedPillarBlock {
    public TimeWoodLog(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Wood Log.
     *
     * @return Block properties with wood-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 2.0f) // hardness, blast resistance (same as Oak Log)
                .sound(SoundType.WOOD)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_log")));
    }
}
