package com.chronosphere.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

/**
 * Unstable Fungus block.
 *
 * A special block found in the Chronosphere dimension that applies random speed effects
 * to entities that collide with it.
 *
 * Properties:
 * - Effect duration: 5 seconds (100 ticks)
 * - Possible effects: Speed I or Slowness I
 * - Effect chance: 100% (always triggers on collision)
 *
 * The random speed effect logic is implemented in the entityInside method.
 *
 * Reference: data-model.md (Blocks → Unstable Fungus)
 */
public class UnstableFungus extends Block {
    private static final Random RANDOM = new Random();
    private static final int EFFECT_DURATION_TICKS = 100; // 5 seconds

    // Bounding box shape for Unstable Fungus
    // Size: 4-12 pixels (width/depth), 0-11 pixels (height)
    // Smaller than warped/crimson fungus (14), closer to regular mushrooms (6)
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 11.0, 12.0);

    public UnstableFungus(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Unstable Fungus.
     *
     * @return Block properties with appropriate settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(0.0f, 0.0f) // Very weak, similar to mushrooms
                .sound(SoundType.FUNGUS)
                .noCollission(); // Entities can walk through it
    }

    /**
     * Get the shape of the block for rendering and collision.
     * Uses a smaller bounding box (8x11x8 pixels).
     *
     * @param state The block state
     * @param level The level
     * @param pos The block position
     * @param context The collision context
     * @return The shape of the block
     */
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Called when an entity collides with this block.
     * Applies a random speed effect (Speed I or Slowness I) to living entities.
     * Only applies if the entity doesn't already have either effect.
     *
     * @param state The block state
     * @param level The level
     * @param pos The block position
     * @param entity The entity that collided
     */
    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        // Only process on server side and for living entities
        if (level.isClientSide() || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        // Check if entity already has Speed or Slowness effect
        // If so, don't apply a new effect (prevents both effects from being active)
        if (livingEntity.hasEffect(MobEffects.MOVEMENT_SPEED) ||
            livingEntity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            return;
        }

        // Apply random speed effect (50% chance for Speed I, 50% for Slowness I)
        MobEffectInstance effect;
        if (RANDOM.nextBoolean()) {
            // Speed I for 5 seconds
            effect = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_DURATION_TICKS, 0);
        } else {
            // Slowness I for 5 seconds
            effect = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION_TICKS, 0);
        }

        livingEntity.addEffect(effect);
    }
}
