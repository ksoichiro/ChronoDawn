package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Time Crystal Block - Decorative block that emits light.
 *
 * This block is crafted from 9 Time Crystals and serves as a decorative light source
 * with a crystalline, time-themed aesthetic.
 *
 * Properties:
 * - Hardness: 3.0 (similar to glass blocks)
 * - Blast Resistance: 3.0 (fragile compared to stone)
 * - Requires pickaxe to mine
 * - Light Level: 10 (bright ambient light, similar to sea lanterns)
 * - Glass-like sound for crystal material
 *
 * Crafting:
 * - 9x Time Crystal → 1x Time Crystal Block (3x3 crafting)
 * - Reversible: 1x Time Crystal Block → 9x Time Crystal (to be defined in recipe)
 *
 * Usage:
 * - Decorative light source for builds
 * - Time-themed alternative to glowstone/sea lanterns
 * - Can be used in combination with other decorative blocks
 *
 * Visual:
 * - Should have a crystalline, glowing appearance
 * - Emits steady light (no flickering)
 *
 * Task: T241 [P] [US1] Create Time Crystal Block
 */
public class TimeCrystalBlock extends Block {
    public TimeCrystalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Crystal Block.
     *
     * @return Block properties with appropriate settings for glowing crystal block
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                .mapColor(MapColor.COLOR_LIGHT_BLUE)  // Light blue color for crystal theme
                .strength(3.0f, 3.0f)                  // Medium hardness, fragile
                // TODO: 1.21.2 - requiresCorrectToolForDrops() causes "Block id not set" error
                // .requiresCorrectToolForDrops()          // Requires pickaxe
                .sound(SoundType.GLASS)                 // Glass-like sound for crystal
                .lightLevel(state -> 10)                // Emits light level 10
                .noOcclusion()                         // Allow transparency like glass
                .setId(ResourceKey.create(Registries.BLOCK,
                        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_crystal_block")));
    }
}
