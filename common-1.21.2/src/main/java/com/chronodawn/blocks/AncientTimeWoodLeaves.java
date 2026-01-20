package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Ancient Time Wood Leaves block.
 *
 * A variant of Time Wood Leaves with a wider, spreading appearance.
 * Found on wide-canopy trees in the ChronoDawn dimension.
 *
 * Properties:
 * - Hardness: 0.2 (same as Oak Leaves)
 * - Transparent: Yes
 * - Decay: Yes (decays when not connected to logs)
 * - Olive/sage green color for ancient appearance
 *
 * Reference: T088t [US1] Design Time Wood color variants
 */
public class AncientTimeWoodLeaves extends LeavesBlock {
    public AncientTimeWoodLeaves(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
            .setValue(DISTANCE, 7)
            .setValue(PERSISTENT, false)
            .setValue(WATERLOGGED, false));
    }

    /**
     * Create default properties for Ancient Time Wood Leaves.
     *
     * @return Block properties with leaves-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_GREEN) // Olive/sage green
                .strength(0.2f)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, entityType) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .pushReaction(PushReaction.DESTROY)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_leaves")));
    }
}
