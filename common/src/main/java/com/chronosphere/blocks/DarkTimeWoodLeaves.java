package com.chronosphere.blocks;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Dark Time Wood Leaves block.
 *
 * A variant of Time Wood Leaves with a darker appearance.
 * Found on tall, ancient trees in the Chronosphere dimension.
 *
 * Properties:
 * - Hardness: 0.2 (same as Oak Leaves)
 * - Transparent: Yes
 * - Decay: Yes (decays when not connected to logs)
 * - Darker color than regular Time Wood Leaves
 *
 * Reference: T088t [US1] Design Time Wood color variants
 */
public class DarkTimeWoodLeaves extends LeavesBlock {
    public DarkTimeWoodLeaves(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
            .setValue(DISTANCE, 7)
            .setValue(PERSISTENT, false)
            .setValue(WATERLOGGED, false));
    }

    /**
     * Create default properties for Dark Time Wood Leaves.
     *
     * @return Block properties with leaves-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY) // Darker leaves
                .strength(0.2f)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, entityType) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .pushReaction(PushReaction.DESTROY);
    }
}
