package com.chronodawn.neoforge.mixin;

import com.chronodawn.neoforge.registry.ModParticles;
import com.chronodawn.registry.ModBlocks;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to override Custom Portal API's particle effects for ChronoDawn portal.
 *
 * Problem: Custom Portal API Reforged uses BlockParticleOption which creates
 * block breaking particles (looks like Clockstone fragments).
 *
 * Solution: Override animateTick() to use custom ChronoDawnPortalParticle
 * which extends vanilla PortalParticle for the "sucking in" movement effect.
 */
@Mixin(CustomPortalBlock.class)
public class CustomPortalBlockMixin {

    /**
     * Inject at the beginning of animateTick to override particle behavior
     * for ChronoDawn portals.
     */
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void onAnimateTick(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        // Get the frame block for this portal using CustomPortalBlock's getPortalBase method
        CustomPortalBlock portalBlock = (CustomPortalBlock) (Object) this;
        net.minecraft.world.level.block.Block frameBlock = portalBlock.getPortalBase(level, pos);

        // Check if this portal uses Clockstone Block as frame (ChronoDawn portal)
        if (frameBlock == ModBlocks.CLOCKSTONE_BLOCK.get()) {
            // This is a ChronoDawn portal - use custom orange dust particles
            spawnChronoDawnParticles(level, pos, random);
            ci.cancel(); // Cancel default particle spawning
        }
        // For other portals, let default behavior run
    }

    /**
     * Spawn ChronoDawn portal particles with "sucking in" movement.
     * Uses custom ChronoDawnPortalParticle which extends vanilla PortalParticle.
     */
    private void spawnChronoDawnParticles(Level level, BlockPos pos, RandomSource random) {
        // Spawn particles (similar frequency to Nether portal)
        for (int i = 0; i < 4; ++i) {
            // Calculate spawn position around the portal
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();

            // Calculate velocity toward portal center (sucking in effect)
            double velocityX = (random.nextDouble() - 0.5) * 0.5;
            double velocityY = (random.nextDouble() - 0.5) * 0.5;
            double velocityZ = (random.nextDouble() - 0.5) * 0.5;

            // Spawn custom ChronoDawn portal particle
            level.addParticle(
                ModParticles.CHRONOSPHERE_PORTAL.get(),
                x, y, z,
                velocityX, velocityY, velocityZ
            );
        }

        // Play ambient portal sound occasionally
        if (random.nextInt(100) == 0) {
            level.playLocalSound(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                SoundEvents.PORTAL_AMBIENT,
                SoundSource.BLOCKS,
                0.5f,
                random.nextFloat() * 0.4f + 0.8f,
                false
            );
        }
    }
}
