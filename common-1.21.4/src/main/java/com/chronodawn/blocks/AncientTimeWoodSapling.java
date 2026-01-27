package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.chronodawn.worldgen.features.AncientTimeWoodTreeFeature;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.Optional;

/**
 * Ancient Time Wood Sapling block.
 *
 * When planted and grown (with bone meal or naturally), grows into an Ancient Time Wood tree.
 * Obtained from Ancient Time Wood Leaves with a low drop chance.
 *
 * Properties:
 * - Can be planted on grass, dirt, farmland
 * - Grows into Ancient Time Wood tree (wide-canopy variant)
 * - Can be accelerated with bone meal
 * - Destroyed by water flow and pistons
 *
 * Drop Source:
 * - Ancient Time Wood Leaves (5% base chance, affected by Fortune)
 *
 * Visual:
 * - Texture: ancient_time_wood_sapling.png (small weathered time-themed plant)
 */
public class AncientTimeWoodSapling extends SaplingBlock {
    private static final TreeGrower ANCIENT_TIME_WOOD_TREE_GROWER = new TreeGrower(
        "ancient_time_wood",
        Optional.empty(), // mega tree feature
        Optional.of(AncientTimeWoodTreeFeature.ANCIENT_TIME_WOOD_TREE),
        Optional.empty()  // secondary mega tree feature
    );

    public AncientTimeWoodSapling(BlockBehaviour.Properties properties) {
        super(ANCIENT_TIME_WOOD_TREE_GROWER, properties);
    }

    /**
     * Create default properties for Ancient Time Wood Sapling.
     *
     * @return Block properties with sapling-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .noOcclusion()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS)
                .pushReaction(PushReaction.DESTROY)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_sapling")));
    }
}
