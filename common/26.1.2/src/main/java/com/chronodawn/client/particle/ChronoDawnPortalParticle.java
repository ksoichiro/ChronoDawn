package com.chronodawn.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * ChronoDawn Portal particle - golden/orange particles that float gently.
 * Similar to Nether Portal particles but with golden/orange color scheme.
 *
 * 1.21.9: TextureSheetParticle was renamed to SingleQuadParticle.
 * Constructor now requires TextureAtlasSprite parameter.
 * getRenderType() split into getGroup() + getLayer().
 */
public class ChronoDawnPortalParticle extends SingleQuadParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;

    protected ChronoDawnPortalParticle(ClientLevel level, double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ,
                                       TextureAtlasSprite sprite) {
        // 1.21.9: SingleQuadParticle constructor requires sprite parameter
        super(level, x, y, z, velocityX, velocityY, velocityZ, sprite);

        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;

        this.xStart = x;
        this.yStart = y;
        this.zStart = z;

        // Random position offset for variety
        this.x += (Math.random() * 2.0 - 1.0) * 0.05;
        this.y += (Math.random() * 2.0 - 1.0) * 0.05;
        this.z += (Math.random() * 2.0 - 1.0) * 0.05;

        // Random velocity dampening
        this.xd *= 0.2;
        this.yd *= 0.2;
        this.zd *= 0.2;

        // Golden/orange color with slight variation
        float colorVariation = (float) (Math.random() * 0.2 + 0.8); // 0.8 to 1.0
        this.rCol = 1.0F * colorVariation;
        this.gCol = 0.7F * colorVariation;
        this.bCol = 0.2F * colorVariation;

        // Particle size (small, similar to Nether Portal)
        // Nether Portal uses very small particles (0.05 to 0.15)
        this.quadSize = (float) (Math.random() * 0.1 + 0.05);

        // Lifetime (40-80 ticks = 2-4 seconds)
        this.lifetime = (int) (Math.random() * 40.0) + 40;

        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Gentle floating motion (similar to Nether Portal)
        float ageRatio = (float) this.age / (float) this.lifetime;
        float driftX = (float) (this.xStart - this.x);
        float driftY = (float) (this.yStart - this.y);
        float driftZ = (float) (this.zStart - this.z);

        // Slow drift back toward starting position
        this.xd += driftX * 0.001;
        this.yd += driftY * 0.001;
        this.zd += driftZ * 0.001;

        // Apply gentle upward drift
        this.yd += 0.002;

        this.move(this.xd, this.yd, this.zd);

        // Velocity dampening
        this.xd *= 0.98;
        this.yd *= 0.98;
        this.zd *= 0.98;

        // Fade out as particle ages
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            // Fade alpha based on age
            this.alpha = 1.0F - ageRatio;
        }
    }

    // 1.21.9: getRenderType() split into getGroup() and getLayer()
    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    /**
     * Particle provider for ChronoDawnPortalParticle.
     */
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        // 1.21.9: createParticle now takes RandomSource parameter
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                        double x, double y, double z,
                                        double velocityX, double velocityY, double velocityZ,
                                        RandomSource random) {
            // 1.21.9: pickSprite replaced with passing sprite via constructor
            TextureAtlasSprite sprite = this.sprites.get(random);
            return new ChronoDawnPortalParticle(
                level, x, y, z, velocityX, velocityY, velocityZ, sprite
            );
        }
    }
}
