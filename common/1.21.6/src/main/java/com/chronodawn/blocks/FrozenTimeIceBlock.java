package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Frozen Time Ice - Special ice block exclusive to snowy biome.
 *
 * This block serves as a decorative ice variant found in ChronoDawn snowy biomes.
 * Unlike vanilla ice, this block does not melt when exposed to light sources,
 * representing time-frozen ice that cannot be thawed.
 *
 * Properties:
 * - Hardness: 0.5 (same as vanilla ice)
 * - Blast Resistance: 0.5 (same as vanilla ice)
 * - Tool: Can be mined with pickaxe (silk touch drops the block itself)
 * - Glass sound (like vanilla ice)
 * - Slippery surface (friction: 0.98, same as ice)
 * - Does NOT melt when near light sources or heat
 * - Semi-transparent
 *
 * Generation:
 * - Exclusive to chronodawn_snowy biome
 * - Replaces water blocks in frozen lakes and rivers
 *
 * Visual:
 * - Ice-like texture with subtle purple/blue tint
 * - Semi-transparent with frozen time theme
 *
 * Task: T245 [P] [US1] Create Frozen Time Ice block
 */
public class FrozenTimeIceBlock extends IceBlock {
    public FrozenTimeIceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Frozen Time Ice.
     *
     * @return Block properties with appropriate settings for non-melting ice
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.ICE)               // Ice color
                .friction(0.98F)                      // Slippery (same as vanilla ice)
                .strength(0.5f, 0.5f)                 // Same as vanilla ice
                .sound(SoundType.GLASS)               // Glass/ice sound
                .noOcclusion()                        // Semi-transparent
                .isValidSpawn((state, level, pos, entityType) -> false)  // Entities can't spawn on ice
                .isRedstoneConductor((state, level, pos) -> false)       // Not redstone conductor
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "frozen_time_ice")));
    }

    /**
     * Override melt behavior to prevent melting.
     * This ice block never melts, regardless of light level or nearby heat sources.
     */
    @Override
    protected void melt(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        // Do nothing - this ice never melts
        // Overrides vanilla IceBlock.melt() to prevent melting behavior
    }
}
