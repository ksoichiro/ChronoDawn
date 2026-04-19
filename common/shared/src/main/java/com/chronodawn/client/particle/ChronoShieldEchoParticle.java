package com.chronodawn.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Chrono Shield Echo particle - blue-violet translucent sprite used for
 * Time Echo visualization (generation burst, active drift, consumption burst).
 *
 * Short-lived (~25 ticks) with a soft drift + gravity pattern suitable for
 * both burst-style emissions and ambient "echo presence" stages.
 *
 * Pre-1.21.9: uses TextureSheetParticle. Versions 1.21.9+ have their own
 * version-specific ChronoShieldEchoParticle extending SingleQuadParticle
 * (renamed from TextureSheetParticle).
 */
public class ChronoShieldEchoParticle extends TextureSheetParticle {
    protected ChronoShieldEchoParticle(ClientLevel level, double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);

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

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
                                        double velocityX, double velocityY, double velocityZ) {
            ChronoShieldEchoParticle particle = new ChronoShieldEchoParticle(
                level, x, y, z, velocityX, velocityY, velocityZ
            );
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
