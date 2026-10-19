package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.support.BoltLineShape;
import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class BoltParticle extends TTParticle {
    private static final int FRAME_COUNT = 16;
    private static final int BOLT_LIFETIME = 3;
    private static final float MIN_ALPHA = 0.2F;
    private static final float WIDTH_DIVISOR = 6.0F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final int SEED_BOUND = 4096;
    private static final int PHASE_MULTIPLES = 8;
    private static final int BEADS_PER_SECTION = 4;
    private static final float HITBOX = 0.02F;
    private static final float SWAY_TIME_DIVISOR = 24.0F;

    private final float beadSize;
    private final int seed;
    private final BoltLineShape shape;

    private BoltParticle(
            ClientLevel level, double x, double y, double z, BoltParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(options.r(), options.g(), options.b());
        this.setSize(HITBOX, HITBOX);
        this.lifetime = BOLT_LIFETIME;
        this.beadSize = options.width() / WIDTH_DIVISOR;
        this.seed = this.random.nextInt(SEED_BOUND);
        double phase = this.random.nextInt(PHASE_MULTIPLES) * Math.PI;
        this.shape =
                new BoltLineShape(x, y, z, options.targetX(), options.targetY(), options.targetZ(), this.random, phase);
        if (this.shape.isEmpty()) {
            remove();
        }
        frame(this.seed % FRAME_COUNT);
    }

    @Override
    protected void update() {
        frame((this.age + (int) this.seed) % FRAME_COUNT);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        if (this.removed || this.shape.isEmpty()) {
            return;
        }
        Vec3 cam = camera.getPosition();
        float fade = Mth.clamp(1.0F - (this.age + partialTick) / this.lifetime, MIN_ALPHA, 1.0F);
        int color = ARGB32.colorFromFloat(fade, this.rCol, this.gCol, this.bCol);
        double amplitude = (this.age + partialTick) / SWAY_TIME_DIVISOR;
        Quaternionf rotation = camera.rotation();
        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        for (int section = 1; section <= this.shape.sections(); section++) {
            for (int bead = 1; bead <= BEADS_PER_SECTION; bead++) {
                float fraction = (float) bead / BEADS_PER_SECTION;
                emitQuad(
                        buffer,
                        rotation,
                        (float) (beadCoordinate(section, fraction, 0, amplitude) - cam.x()),
                        (float) (beadCoordinate(section, fraction, 1, amplitude) - cam.y()),
                        (float) (beadCoordinate(section, fraction, 2, amplitude) - cam.z()),
                        this.beadSize,
                        u0,
                        u1,
                        v0,
                        v1,
                        color,
                        EMISSIVE_LIGHT);
            }
        }
    }

    private double beadCoordinate(int section, double fraction, int axis, double amplitude) {
        return Mth.lerp(
                fraction,
                this.shape.coordinate(section - 1, axis, amplitude),
                this.shape.coordinate(section, axis, amplitude));
    }

    private static void emitQuad(
            VertexConsumer buffer,
            Quaternionf rotation,
            float x,
            float y,
            float z,
            float size,
            float u0,
            float u1,
            float v0,
            float v1,
            int color,
            int light) {
        emitVertex(buffer, rotation, x, y, z, 1.0F, -1.0F, size, u1, v1, color, light);
        emitVertex(buffer, rotation, x, y, z, 1.0F, 1.0F, size, u1, v0, color, light);
        emitVertex(buffer, rotation, x, y, z, -1.0F, 1.0F, size, u0, v0, color, light);
        emitVertex(buffer, rotation, x, y, z, -1.0F, -1.0F, size, u0, v1, color, light);
    }

    private static void emitVertex(
            VertexConsumer buffer,
            Quaternionf rotation,
            float x,
            float y,
            float z,
            float cornerX,
            float cornerY,
            float size,
            float u,
            float v,
            int color,
            int light) {
        Vector3f pos =
                new Vector3f(cornerX, cornerY, 0.0F).rotate(rotation).mul(size).add(x, y, z);
        buffer.addVertex(pos.x(), pos.y(), pos.z()).setUv(u, v).setColor(color).setLight(light);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.beadSize;
    }

    public static final class Provider implements ParticleProvider<BoltParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("bolt");

        @Override
        public Particle createParticle(
                BoltParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            return new BoltParticle(level, x, y, z, options, SHEET);
        }
    }
}
