package com.chronodawn.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Temporal Particle Emitter - Invisible block that emits time distortion particles.
 *
 * This block is used to create visual effects in Ancient Ruins structures,
 * representing the failing Temporal Seal and temporal energy leakage.
 *
 * Properties:
 * - Completely invisible (no model, no collision)
 * - Cannot be broken (indestructible)
 * - Emits portal particles continuously (client-side)
 * - No light emission
 * - Intended for structure placement only (not obtainable in survival)
 *
 * Lore Context:
 * - Represents Temporal Seal degradation
 * - Visual indicator that "something is wrong" with time
 * - Particles float upward from floor blocks
 * - Only appears in Ancient Ruins (Overworld)
 *
 * Task: T115m [US2] Implement time distortion particle effects for Ancient Ruins
 * Reference: specs/chrono-dawn-mod/lore.md
 */
public class TemporalParticleEmitterBlock extends Block {
    // Empty shape - no collision
    private static final VoxelShape SHAPE = Shapes.empty();

    public TemporalParticleEmitterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Temporal Particle Emitter.
     *
     * @return Block properties configured for invisible, indestructible particle emitter
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)              // No color on maps
                .strength(-1.0f, 3600000.0f)          // Indestructible (like bedrock)
                .noCollission()                       // No collision box
                .noOcclusion()                        // Does not block light
                .air()                                // Behaves like air
                .pushReaction(PushReaction.BLOCK);    // Cannot be pushed by pistons
    }

    /**
     * Client-side particle animation.
     * Spawns particles that float upward, simulating temporal energy leakage.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Spawn particles with 70% chance per tick (14 times per second on average)
        if (random.nextFloat() < 0.7f) {
            // Particle position: random offset within block
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.3; // Start near bottom of block
            double z = pos.getZ() + random.nextDouble();

            // Particle velocity: float upward with increased speed
            double velocityX = (random.nextDouble() - 0.5) * 0.02; // Slight horizontal drift
            double velocityY = random.nextDouble() * 0.1 + 0.15;   // Faster upward motion (0.15-0.25)
            double velocityZ = (random.nextDouble() - 0.5) * 0.02; // Slight horizontal drift

            // Spawn SOUL_FIRE_FLAME particle (cyan-blue flame, fits time theme)
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    /**
     * No collision shape - players/entities pass through this block.
     */
    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * No collision shape for entities.
     */
    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Invisible rendering - no model displayed.
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /**
     * This block does not propagate skylight downward.
     */
    @Override
    public boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return true;
    }

    /**
     * This block is air-like and replaceable.
     */
    @Override
    public boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) {
        return true;
    }
}
