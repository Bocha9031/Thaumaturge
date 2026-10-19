package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.FireMoteParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.RandomSource;

public final class FireMoteParticle extends TTParticle {
    private static final int LIFETIME = 14;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final int EXTRA_AGE_ODDS = 5;
    private static final float SPIN_PER_TICK = 0.32F;
    private static final float START_ROLL = (float) (Math.PI / 6.0);

    private final float startAlpha;
    private final float startSize;
    private final boolean translucent;

    private FireMoteParticle(
            ClientLevel level, double x, double y, double z, FireMoteParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, options.vx(), options.vy(), options.vz(), sheet);
        this.rCol = normalize(options.r());
        this.gCol = normalize(options.g());
        this.bCol = normalize(options.b());
        this.startAlpha = options.alpha();
        this.alpha = this.startAlpha;
        this.lifetime = LIFETIME;
        this.startSize = options.scale() * 0.1F;
        this.quadSize = this.startSize;
        this.translucent = options.translucent();
        this.roll = START_ROLL;
        this.oRoll = this.roll;
    }

    private static float normalize(float channel) {
        return channel > 1.0F ? channel / 255.0F : channel;
    }

    @Override
    protected void update() {
        if (this.random.nextInt(EXTRA_AGE_ODDS) == 0) {
            this.age++;
        }
        this.oRoll = this.roll;
        float t = progress();
        this.quadSize = this.startSize * (1.0F - t);
        this.alpha = this.startAlpha * (1.0F - t);
        this.roll += SPIN_PER_TICK;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return EMISSIVE_LIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.translucent ? TTParticleLayers.translucent(this.sheet) : TTParticleLayers.additive(this.sheet);
    }

    public static final class Provider implements ParticleProvider<FireMoteParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("fire_mote");

        @Override
        public Particle createParticle(
                FireMoteParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            RandomSource random = level.getRandom();
            return new FireMoteParticle(level, x, y, z, options, SHEET);
        }
    }
}
