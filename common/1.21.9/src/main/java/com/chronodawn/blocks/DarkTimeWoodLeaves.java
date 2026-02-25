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
 * Dark Time Wood Leaves block.
 *
 * A variant of Time Wood Leaves with a darker appearance.
 * Found on tall, ancient trees in the ChronoDawn dimension.
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
    // Codec for serialization (required in 1.21.5)
    public static final MapCodec<DarkTimeWoodLeaves> CODEC = simpleCodec(DarkTimeWoodLeaves::new);

    // Chance for falling leaves particles (same as vanilla oak leaves)
    private static final float FALLING_LEAVES_PARTICLE_CHANCE = 0.01f;

    public DarkTimeWoodLeaves(BlockBehaviour.Properties properties) {
        super(FALLING_LEAVES_PARTICLE_CHANCE, properties);
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
        // Empty implementation - no special particles
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
                .pushReaction(PushReaction.DESTROY)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_leaves")));
    }
}
