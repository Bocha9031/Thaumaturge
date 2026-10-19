package com.leclowndu93150.thaumaturge.client.casters;

final class RadialFocusMotion {
    private RadialFocusMotion() {}

    static float change(long now, long previous, long duration, long maxFrameDelta) {
        if (duration <= 0L || maxFrameDelta <= 0L) {
            return 0.0F;
        }
        long elapsed = Math.max(0L, Math.min(now - previous, maxFrameDelta));
        return (float) elapsed / duration;
    }
}
