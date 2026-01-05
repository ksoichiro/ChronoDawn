package com.chronodawn.fabric.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Custom portal particle for ChronoDawn dimension.
 *
 * Implements the "sucking in" movement behavior like vanilla Nether portal,
 * with ChronoDawn's orange color theme (#db8813).
 *
 * The particle spawns at an offset position and moves toward the portal center,
 * creating the visual effect of being pulled into the portal.
 */
public class ChronoDawnPortalParticle extends TextureSheetParticle {

    /**
     * ChronoDawn portal color - Orange (#db8813).
     * RGB normalized to 0-1 range.
     */
    private static final float COLOR_R = 219f / 255f; // 0.859
    private static final float COLOR_G = 136f / 255f; // 0.533
    private static final float COLOR_B = 19f / 255f;  // 0.075

    /**
     * Starting position - the particle moves toward this point (sucking effect).
     */
    private final double xStart;
    private final double yStart;
    private final double zStart;

    protected ChronoDawnPortalParticle(ClientLevel level, double x, double y, double z,
                                          double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z);

        // Set movement direction (particle will move in this direction initially)
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        // Offset the starting position by the speed values
        // This places the particle away from the portal
        this.x = x + xSpeed;
        this.y = y + ySpeed;
        this.z = z + zSpeed;

        // Record the original spawn point - particle will move toward this
        this.xStart = x;
        this.yStart = y;
        this.zStart = z;

        // Scale the movement speed (similar to vanilla PortalParticle)
        this.xd = xSpeed * 0.1;
        this.yd = ySpeed * 0.1;
        this.zd = zSpeed * 0.1;

        // Apply ChronoDawn orange color
        this.rCol = COLOR_R;
        this.gCol = COLOR_G;
        this.bCol = COLOR_B;

        // Particle size (similar to vanilla portal particle)
        this.quadSize *= 0.75f;

        // Lifetime (40-50 ticks, like vanilla)
        this.lifetime = (int)(Math.random() * 10.0) + 40;
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        // Fade in/out based on age
        float progress = ((float)this.age + scaleFactor) / (float)this.lifetime;
        progress = 1.0f - progress;
        progress = progress * progress;
        progress = 1.0f - progress;
        return this.quadSize * progress;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            // Calculate progress through lifetime
            float progress = (float)this.age / (float)this.lifetime;

            // Move toward the starting point (sucking in effect)
            // The (1 - progress) factor causes the particle to slow down as it approaches center
            this.x += this.xd * (1.0 - progress);
            this.y += this.yd;
            this.z += this.zd * (1.0 - progress);

            // Additional pull toward center (stronger sucking effect)
            double pullStrength = 0.02 * (1.0 - progress);
            this.x += (this.xStart - this.x) * pullStrength;
            this.y += (this.yStart - this.y) * pullStrength;
            this.z += (this.zStart - this.z) * pullStrength;
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        // Make particle glow slightly (self-illuminated)
        int light = super.getLightColor(partialTick);
        int blockLight = light & 0xFF;
        int skyLight = (light >> 16) & 0xFF;

        // Boost block light for glow effect
        blockLight = Math.max(blockLight, 240);

        return blockLight | (skyLight << 16);
    }

    @Override
    public ParticleRenderType getRenderType() {
        // Use translucent rendering for portal-like effect
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /**
     * Factory for creating ChronoDawnPortalParticle instances.
     * Used by the particle system to spawn new particles.
     */
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                        double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed) {
            ChronoDawnPortalParticle particle = new ChronoDawnPortalParticle(
                level, x, y, z, xSpeed, ySpeed, zSpeed
            );
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
