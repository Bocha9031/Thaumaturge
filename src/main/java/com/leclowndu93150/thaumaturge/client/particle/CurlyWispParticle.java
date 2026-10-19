package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.CurlyWispParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;

public final class CurlyWispParticle extends TTParticle {
    private static final int FRAME_COUNT = 4;
    private static final int LIFETIME_BASE = 20;
    private static final int LIFETIME_RANGE_BASE = 20;
    private static final int LIFETIME_RANGE_PER_SEED = 4;
    private static final float START_SIZE_FACTOR = 0.5F;
    private static final float END_SIZE_BASE = 1.05F;
    private static final float END_SIZE_RANGE = 0.25F;
    private static final float SPIN_BASE = 1.1F;
    private static final float SPIN_RANGE = 0.6F;
    private static final double TILT_DEVIATION_DEGREES = 55.0;
    private static final float END_R = 0.09F;
    private static final float END_G = 0.02F;
    private static final float END_B = 0.13F;
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final float startR;
    private final float startG;
    private final float startB;
    private final float startAlpha;
    private final float startSize;
    private final float endSize;
    private final FacingCameraMode facing;

    private CurlyWispParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            CurlyWispParticleOptions options,
            ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.startR = ARGB32.red(options.color()) / 255.0F;
        this.startG = ARGB32.green(options.color()) / 255.0F;
        this.startB = ARGB32.blue(options.color()) / 255.0F;
        this.startAlpha = options.alpha();
        this.lifetime =
                LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE_BASE + LIFETIME_RANGE_PER_SEED * options.seed());
        this.startSize = START_SIZE_FACTOR * options.scale();
        this.endSize = (END_SIZE_BASE + this.random.nextFloat() * END_SIZE_RANGE) * options.scale();
        this.quadSize = this.startSize;
        frame(this.random.nextInt(FRAME_COUNT));
        float spinSpeed = SPIN_BASE + this.random.nextFloat() * SPIN_RANGE;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -spinSpeed : spinSpeed);
        setDelay(options.delay());
        if (options.seed() > 0 && this.random.nextBoolean()) {
            float yaw = (float) TILT_DEVIATION_DEGREES * (float) this.random.nextGaussian() * DEG_TO_RAD;
            float pitch = (float) TILT_DEVIATION_DEGREES * (float) this.random.nextGaussian() * DEG_TO_RAD;
            this.facing = (target, camera, partialTick) -> target.rotationYXZ(-yaw, pitch, 0.0F);
        } else {
            this.facing = FacingCameraMode.LOOKAT_XYZ;
        }
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = this.startAlpha * (1.0F - t);
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
        lerpColor(t, this.startR, this.startG, this.startB, END_R, END_G, END_B);
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return this.facing;
    }

    public static final class Provider implements ParticleProvider<CurlyWispParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("curly_wisp");

        @Override
        public Particle createParticle(
                CurlyWispParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            RandomSource random = level.getRandom();
            return new CurlyWispParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
