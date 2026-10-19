package com.leclowndu93150.thaumaturge.client.casters;

final class RadialFocusMotion {
    private static final float SCALE_MIN = 0.0F;
    private static final float SCALE_MAX = 1.0F;

    private RadialFocusMotion() {}

    static float change(long now, long previous, long duration, long maxFrameDelta) {
        if (duration <= 0L || maxFrameDelta <= 0L) {
            return 0.0F;
        }
        long elapsed = Math.max(0L, Math.min(now - previous, maxFrameDelta));
        return (float) elapsed / duration;
    }

    static float easeOutCubic(float scale) {
        float inverse = SCALE_MAX - Math.max(SCALE_MIN, Math.min(scale, SCALE_MAX));
        return SCALE_MAX - inverse * inverse * inverse;
    }
}
