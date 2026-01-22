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
 * Stripped Ancient Time Wood Log block.
 *
 * Stripped variant of Ancient Time Wood Log, obtained by right-clicking with an axe.
 * Has bark removed, showing the inner wood texture.
 *
 * Properties:
 * - Hardness: 2.0 (same as Oak Log)
 * - Blast Resistance: 2.0
 * - Flammable: Yes
 * - Directional: Can be placed in X, Y, Z axis
 * - Weathered, grayish-brown color
 *
 * Visual:
 * - Side texture: stripped_ancient_time_wood_log.png (weathered inner wood without bark)
 * - Top texture: stripped_ancient_time_wood_log_top.png (weathered time-themed rings)
 *
 * Reference: T719 [Add axe stripping functionality for Time Wood logs]
 */
public class StrippedAncientTimeWoodLog extends RotatedPillarBlock {
    public StrippedAncientTimeWoodLog(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Stripped Ancient Time Wood Log.
     *
     * @return Block properties with wood-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN) // Weathered brown color
                .strength(2.0f, 2.0f)
                .sound(SoundType.WOOD)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "stripped_ancient_time_wood_log")));
    }
}
