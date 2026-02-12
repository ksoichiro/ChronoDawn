package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Time Wood Leaves block.
 *
 * Custom leaves block used in Fruit of Time trees found in the ChronoDawn dimension.
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
    // Codec for serialization (required in 1.21.5)
    public static final MapCodec<TimeWoodLeaves> CODEC = simpleCodec(TimeWoodLeaves::new);

    // Chance for falling leaves particles (same as vanilla oak leaves)
    private static final float FALLING_LEAVES_PARTICLE_CHANCE = 0.01f;

    public TimeWoodLeaves(BlockBehaviour.Properties properties) {
        super(FALLING_LEAVES_PARTICLE_CHANCE, properties);
        // Register default state with DISTANCE=7, PERSISTENT=false, WATERLOGGED=false
        // This is inherited from LeavesBlock and set automatically
        registerDefaultState(this.stateDefinition.any()
            .setValue(DISTANCE, 7)
            .setValue(PERSISTENT, false)
            .setValue(WATERLOGGED, false));
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    /**
     * Spawns falling leaf particles. In 1.21.5, this became an abstract method.
     */
    @Override
    public void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        // Use vanilla cherry leaves particle for now (or custom particle in future)
        // Spawns nothing by default - subclasses can override for custom effects
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
                .pushReaction(PushReaction.DESTROY) // Destroyed by pistons
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_leaves")));
    }
}
