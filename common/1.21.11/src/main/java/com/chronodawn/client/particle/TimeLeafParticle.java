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
 * Ambient leaf particle for the Time Wood leaf blocks.
 *
 * One class covers three motions so every leaf kind shares the same sway and
 * lifetime handling:
 * - FALLING: drifts down with a gentle side-to-side sway (Time Wood, Dark Time Wood)
 * - RISING: drifts up and fades out (Ancient Time Wood)
 * - GLOWING_FALLING: falls at half speed at full brightness (night petals)
 *
 * 1.21.9+: extends SingleQuadParticle (renamed from TextureSheetParticle).
 * Uses getGroup() + getLayer() (split from getRenderType() in 1.21.9).
 */
public class TimeLeafParticle extends SingleQuadParticle {
    public enum Motion {
        FALLING,
        RISING,
        GLOWING_FALLING
    }

    private static final double FALL_SPEED = 0.03;
    private static final double RISE_SPEED = 0.025;
    private static final double SWAY_AMPLITUDE = 0.02;
    private static final float SWAY_FREQUENCY = 0.08F;
    private static final int FULL_BRIGHT = 0xF000F0;

    private final Motion motion;
    private final double verticalSpeed;
    private final float swayPhase;
    private final float spin;

    protected TimeLeafParticle(ClientLevel level, double x, double y, double z, Motion motion,
                               TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        this.motion = motion;

        // 100-160 ticks so neighbouring leaves do not disappear in lockstep
        this.lifetime = 100 + this.random.nextInt(61);
        this.verticalSpeed = switch (motion) {
            case FALLING -> -FALL_SPEED;
            case RISING -> RISE_SPEED;
            case GLOWING_FALLING -> -FALL_SPEED / 2.0;
        };
        this.swayPhase = this.random.nextFloat() * (float) (Math.PI * 2.0);
        this.spin = (this.random.nextFloat() - 0.5F) * 0.1F;
        this.roll = this.random.nextFloat() * (float) (Math.PI * 2.0);
        this.oRoll = this.roll;

        this.xd = 0.0;
        this.yd = this.verticalSpeed;
        this.zd = 0.0;

        // Falling leaves come to rest on the ground. Rising leaves pass through the canopy above.
        this.hasPhysics = motion != Motion.RISING;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        if (this.onGround) {
            // Rest on the ground instead of sliding around
            this.xd = 0.0;
            this.yd = 0.0;
            this.zd = 0.0;
        } else {
            float angle = this.age * SWAY_FREQUENCY + this.swayPhase;
            this.xd = Math.sin(angle) * SWAY_AMPLITUDE;
            this.zd = Math.cos(angle * 0.7F) * SWAY_AMPLITUDE;
            this.yd = this.verticalSpeed;
            this.roll += this.spin;
        }
        this.move(this.xd, this.yd, this.zd);

        if (this.motion == Motion.RISING) {
            this.alpha = 1.0F - (float) this.age / (float) this.lifetime;
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        if (this.motion == Motion.GLOWING_FALLING) {
            return FULL_BRIGHT;
        }
        return super.getLightColor(partialTick);
    }

    // 1.21.9+: getRenderType() split into getGroup() and getLayer()
    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        // RISING fades its alpha and GLOWING_FALLING is a soft petal, so both need blending
        if (this.motion == Motion.FALLING) {
            return SingleQuadParticle.Layer.OPAQUE;
        }
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    /**
     * Particle provider for TimeLeafParticle.
     */
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Motion motion;

        public Provider(SpriteSet sprites, Motion motion) {
            this.sprites = sprites;
            this.motion = motion;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ,
                                       RandomSource random) {
            TextureAtlasSprite sprite = this.sprites.get(random);
            return new TimeLeafParticle(level, x, y, z, this.motion, sprite);
        }
    }
}
