package com.chronosphere.blocks;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Time Wood Leaves block.
 *
 * Custom leaves block used in Fruit of Time trees found in the Chronosphere dimension.
 * Features time-themed textures and supports fruit growth decoration.
 *
 * Properties:
 * - Hardness: 0.2 (same as Oak Leaves)
 * - Blast Resistance: 0.2
 * - Transparent: Yes (allows light through)
 * - Decay: Yes (decays when not connected to logs)
 * - Distance: Tracks distance from log blocks (1-7)
 *
 * Behavior:
 * - LeavesBlock provides distance tracking and decay logic
 * - Leaves with distance=7 and persistent=false will decay
 * - Leaves connected to logs (distance<7) won't decay
 *
 * Visual:
 * - Texture: time_wood_leaves.png (time-themed leaves, possibly with glow effect)
 * - Optional: Emissive glow overlay for magical appearance
 *
 * Future Enhancements:
 * - Custom particle effects (time distortion particles)
 * - Fruit of Time blocks attached via decorator
 *
 * Reference: T080f [US1] Create Time Wood Leaves block
 * Related: FruitOfTimeTreeFeature uses this for tree generation
 */
public class TimeWoodLeaves extends LeavesBlock {
    public TimeWoodLeaves(BlockBehaviour.Properties properties) {
        super(properties);
        // Register default state with DISTANCE=7, PERSISTENT=false, WATERLOGGED=false
        // This is inherited from LeavesBlock and set automatically
        registerDefaultState(this.stateDefinition.any()
            .setValue(DISTANCE, 7)
            .setValue(PERSISTENT, false)
            .setValue(WATERLOGGED, false));
    }

    /**
     * Create default properties for Time Wood Leaves.
     *
     * @return Block properties with leaves-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.2f) // hardness and blast resistance
                .randomTicks() // Allows decay mechanics
                .sound(SoundType.GRASS)
                .noOcclusion() // Transparent block
                .isValidSpawn((state, level, pos, entityType) -> false) // Mobs can't spawn on leaves
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .pushReaction(PushReaction.DESTROY); // Destroyed by pistons
    }
}
