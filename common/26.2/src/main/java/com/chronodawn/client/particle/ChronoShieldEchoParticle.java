package com.chronodawn.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * Chrono Shield Echo particle - blue-violet translucent sprite used for
 * Time Echo visualization (generation burst, active drift, consumption burst).
 *
 * Short-lived (~25 ticks) with a soft drift + gravity pattern suitable for
 * both burst-style emissions and ambient "echo presence" stages.
 *
 * 1.21.11: extends SingleQuadParticle (renamed from TextureSheetParticle in 1.21.9).
 * Uses getGroup() + getLayer() (split from getRenderType() in 1.21.9).
 */
public class ChronoShieldEchoParticle extends SingleQuadParticle {
    protected ChronoShieldEchoParticle(ClientLevel level, double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ,
                                       TextureAtlasSprite sprite) {
        super(level, x, y, z, velocityX, velocityY, velocityZ, sprite);

        // Blue-violet tint
        this.rCol = 0.55F;
        this.gCol = 0.35F;
        this.bCol = 0.95F;

        this.lifetime = 25;

        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;

        this.scale(1.2F);
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Slight gravity
        this.yd -= 0.004;
        this.move(this.xd, this.yd, this.zd);

        // Dampening for soft drift
        this.xd *= 0.92;
        this.yd *= 0.92;
        this.zd *= 0.92;

        // Fade out as the particle ages
        this.alpha = 1.0F - (float) this.age / (float) this.lifetime;
    }

    // 1.21.9+: getRenderType() split into getGroup() and getLayer()
    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    /**
     * Particle provider for ChronoShieldEchoParticle.
     */
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ,
                                       RandomSource random) {
            TextureAtlasSprite sprite = this.sprites.get(random);
            return new ChronoShieldEchoParticle(
                level, x, y, z, velocityX, velocityY, velocityZ, sprite
            );
        }
    }
}
