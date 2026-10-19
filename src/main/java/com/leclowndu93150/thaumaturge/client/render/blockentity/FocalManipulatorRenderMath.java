package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.util.Mth;

final class FocalManipulatorRenderMath {
    private static final int BEAM_COUNT = 2;
    private static final int PROPERTY_COUNT = 3;
    private static final int HASH_SHIFT = 8;
    private static final float HASH_RANGE = 1.0F / (1 << 24);
    private static final float FOCUS_BOB_PERIOD = 70.0F;
    private static final float FOCUS_BOB_HEIGHT = 0.035F;
    private static final float FOCUS_SPIN = 1.6F;
    private static final float ORBIT_SPIN_RATIO = 0.5F;
    private static final float ORBIT_RADIUS = 0.36F;
    private static final float CRYSTAL_BOB_PERIOD = 46.0F;
    private static final float CRYSTAL_BOB_HEIGHT = 0.025F;
    private static final float CRYSTAL_PHASE_STEP = 1.3F;
    private static final float BEAM_SWAY_PERIOD = 90.0F;
    private static final float BEAM_SWAY = 14.0F;
    private static final float BEAM_TILT_PERIOD = 130.0F;
    private static final float BEAM_TILT = 10.0F;
    private static final float BEAM_WIDTH_PERIOD = 60.0F;
    private static final float BEAM_WIDTH_SWING = 0.35F;
    private static final float BEAM_SPREAD = 22.0F;
    private static final float BEAM_ROLL_RANGE = 180.0F;
    private static final float BEAM_MIN_LENGTH = 0.22F;
    private static final float BEAM_LENGTH_RANGE = 0.16F;
    private static final float BEAM_TIP_HALF = 0.07F;

    private FocalManipulatorRenderMath() {}

    static float focusBob(float ticks) {
        return Mth.sin(ticks * Mth.TWO_PI / FOCUS_BOB_PERIOD) * FOCUS_BOB_HEIGHT;
    }

    static float focusSpin(float ticks) {
        return ticks * FOCUS_SPIN % 360.0F;
    }

    static float orbitDegrees(float ticks, int index, int count) {
        return (ticks * FOCUS_SPIN * ORBIT_SPIN_RATIO + index * 360.0F / count) % 360.0F;
    }

    static float orbitRadius() {
        return ORBIT_RADIUS;
    }

    static float crystalHeight(float ticks, int index) {
        return 1.32F
                + Mth.sin(ticks * Mth.TWO_PI / CRYSTAL_BOB_PERIOD + index * CRYSTAL_PHASE_STEP) * CRYSTAL_BOB_HEIGHT;
    }

    static Beam beam(int seed, int crystal, int beam, float ticks) {
        float side = beam == 0 ? -1.0F : 1.0F;
        float phase = unit(seed, crystal, beam, 2) * Mth.TWO_PI;
        float yaw = side * BEAM_SPREAD + Mth.sin(ticks * Mth.TWO_PI / BEAM_SWAY_PERIOD + phase) * BEAM_SWAY;
        float tilt = Mth.cos(ticks * Mth.TWO_PI / BEAM_TILT_PERIOD + phase) * BEAM_TILT;
        float roll = unit(seed, crystal, beam, 1) * BEAM_ROLL_RANGE;
        float length = BEAM_MIN_LENGTH + unit(seed, crystal, beam, 0) * BEAM_LENGTH_RANGE;
        float tipHalf =
                BEAM_TIP_HALF * (1.0F + Mth.sin(ticks * Mth.TWO_PI / BEAM_WIDTH_PERIOD - phase) * BEAM_WIDTH_SWING);
        return new Beam(yaw, tilt, roll, length, tipHalf);
    }

    private static float unit(int seed, int crystal, int beam, int property) {
        int key = (crystal * BEAM_COUNT + beam) * PROPERTY_COUNT + property;
        return (Mth.murmurHash3Mixer(seed + Mth.murmurHash3Mixer(key)) >>> HASH_SHIFT) * HASH_RANGE;
    }

    record Beam(float yawDegrees, float tiltDegrees, float rollDegrees, float length, float tipHalfWidth) {}
}
