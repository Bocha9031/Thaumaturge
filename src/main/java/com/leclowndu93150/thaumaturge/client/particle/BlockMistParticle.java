package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

public final class BlockMistParticle extends TTParticle {
    private static final int LIFETIME_BASE = 50;
    private static final int LIFETIME_RANGE = 25;
    private static final float GRAVITY = 0.1F;
    private static final float START_SIZE = 0.5F;
    private static final float END_SIZE = 0.1F;
    private static final float PEAK_ALPHA = 0.5F;
    private static final float[] FADE_CURVE = {
        0.0F, PEAK_ALPHA, PEAK_ALPHA * 0.8F, PEAK_ALPHA * 0.6F, PEAK_ALPHA * 0.4F, PEAK_ALPHA * 0.2F, 0.0F
    };
    private static final float SPIN_SPEED = 1.0F;
    private static final double WIND_SCALE = 1.0E-3;

    private BlockMistParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            ColorParticleOption options,
            ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.getRed(), options.getGreen(), options.getBlue());
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.gravity = GRAVITY;
        this.quadSize = START_SIZE;
        this.alpha = 0.0F;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? SPIN_SPEED : -SPIN_SPEED);
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = Keyframes.sample(t, FADE_CURVE);
        this.quadSize = Keyframes.sample(t, START_SIZE, END_SIZE);
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("block_mist");

        @Override
        public Particle createParticle(
                ColorParticleOption options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            RandomSource random = level.getRandom();
            return new BlockMistParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
