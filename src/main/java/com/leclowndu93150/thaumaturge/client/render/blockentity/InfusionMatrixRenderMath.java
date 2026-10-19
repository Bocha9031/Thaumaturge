package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.util.Mth;

final class InfusionMatrixRenderMath {
    static final int SALT_X = 0;
    static final int SALT_Y = 1;
    static final int SALT_Z = 2;
    static final int SALT_LENGTH = 3;
    static final int SALT_WIDTH = 4;
    static final int SALT_PHASE = 5;
    static final int SALT_STRIDE = 8;
    static final int HASH_SHIFT = 8;
    static final float HASH_RANGE = 1 << 24;

    private static final float DRIFT_PERIOD_MIN = 28.0F;
    private static final float DRIFT_PERIOD_SPREAD = 22.0F;
    private static final int DRIFT_INDEX_BASE = 8;
    private static final float INSTABILITY_CAP = 8.0F;
    private static final float CRAFT_SWAY = 2.2F;
    private static final float UNSTABLE_GAIN = 0.12F;
    private static final float INSTABILITY_RAMP_TICKS = 50.0F;
    private static final float GLOW_BASE = 0.32F;
    private static final float GLOW_SWING = 0.14F;
    private static final float GLOW_PERIOD = 56.0F;
    private static final float GLOW_PHASE_STEP = 0.8F;
    private static final int SPIKE_COUNT = 40;
    private static final int SPIKE_INDEX_BASE = 16;
    private static final int SPIKE_CORNERS = 3;
    private static final int AXES = 3;
    private static final int SPIKE_STRIDE = AXES + AXES * SPIKE_CORNERS;
    private static final float SPIKE_REACH = 1.6F;
    private static final float SPIKE_SHORTEST = 0.45F;
    private static final float SPIKE_WIDTH_MIN = 0.025F;
    private static final float SPIKE_WIDTH_SPREAD = 0.035F;
    private static final float VERTICAL_AXIS_LIMIT = 0.9F;

    private InfusionMatrixRenderMath() {}

    static float unit(int index, int salt) {
        return (Mth.murmurHash3Mixer(Mth.murmurHash3Mixer(index * SALT_STRIDE + salt)) >>> HASH_SHIFT) / HASH_RANGE;
    }

    static float drift(float time, int index, int salt) {
        int key = DRIFT_INDEX_BASE + index;
        float period = DRIFT_PERIOD_MIN + DRIFT_PERIOD_SPREAD * unit(key, salt);
        return Mth.sin(Mth.TWO_PI * (time / period + unit(key, SALT_PHASE + salt)));
    }

    static int quarterTurns(int index, int salt) {
        return (int) (unit(index, salt) * 4.0F);
    }

    static float instability(float stability, int craftTicks) {
        float target =
                stability >= 0.0F ? CRAFT_SWAY : Math.min(INSTABILITY_CAP, CRAFT_SWAY - stability * UNSTABLE_GAIN);
        float ramp = Math.min(craftTicks, INSTABILITY_RAMP_TICKS) / INSTABILITY_RAMP_TICKS;
        return Mth.lerp(ramp, 1.0F, target);
    }

    static float glow(float time, float startUp, int index) {
        return startUp * (GLOW_BASE + GLOW_SWING * Mth.sin(Mth.TWO_PI * time / GLOW_PERIOD + index * GLOW_PHASE_STEP));
    }

    static float[] buildSpikes() {
        float[] spikes = new float[SPIKE_COUNT * SPIKE_STRIDE];
        for (int spike = 0; spike < SPIKE_COUNT; spike++) {
            int key = SPIKE_INDEX_BASE + spike;
            float dy = 1.0F - 2.0F * unit(key, SALT_Y);
            float ring = Mth.sqrt(Math.max(0.0F, 1.0F - dy * dy));
            float heading = Mth.TWO_PI * unit(key, SALT_X);
            float dx = Mth.cos(heading) * ring;
            float dz = Mth.sin(heading) * ring;
            float length = Mth.lerp(unit(key, SALT_LENGTH), SPIKE_SHORTEST, 1.0F);
            float width = (SPIKE_WIDTH_MIN + SPIKE_WIDTH_SPREAD * unit(key, SALT_WIDTH)) / SPIKE_REACH;
            boolean steep = Math.abs(dy) > VERTICAL_AXIS_LIMIT;
            float helperX = steep ? 1.0F : 0.0F;
            float helperY = steep ? 0.0F : 1.0F;
            float px = helperY * dz;
            float py = -helperX * dz;
            float pz = helperX * dy - helperY * dx;
            float pLength = Mth.sqrt(px * px + py * py + pz * pz);
            px /= pLength;
            py /= pLength;
            pz /= pLength;
            float qx = dy * pz - dz * py;
            float qy = dz * px - dx * pz;
            float qz = dx * py - dy * px;
            int base = spike * SPIKE_STRIDE;
            spikes[base] = dx;
            spikes[base + 1] = dy;
            spikes[base + 2] = dz;
            for (int corner = 0; corner < SPIKE_CORNERS; corner++) {
                float turn = Mth.TWO_PI * corner / SPIKE_CORNERS;
                float across = Mth.cos(turn) * width;
                float along = Mth.sin(turn) * width;
                int at = base + AXES + corner * AXES;
                spikes[at] = dx * length + px * across + qx * along;
                spikes[at + 1] = dy * length + py * across + qy * along;
                spikes[at + 2] = dz * length + pz * across + qz * along;
            }
        }
        return spikes;
    }

    static int spikeCount() {
        return SPIKE_COUNT;
    }

    static int spikeStride() {
        return SPIKE_STRIDE;
    }
}
