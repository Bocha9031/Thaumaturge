package com.leclowndu93150.thaumaturge.client.model;

import net.minecraft.util.Mth;

final class WandLightMath {
    private static final int MAX_LIGHT_LEVEL = 240;
    private static final int FOCUS_BASE = 218;
    private static final int FOCUS_AMPLITUDE = 10;
    private static final float FOCUS_PERIOD = 3.0F;
    private static final int GLOW_BASE = 226;
    private static final int GLOW_AMPLITUDE = 5;
    private static final float GLOW_RATE = 2.7F;
    private static final float GLOW_DRIFT_RATE = 1.13F;

    private WandLightMath() {}

    static int focus(float ticks) {
        int level = Math.min(MAX_LIGHT_LEVEL, FOCUS_BASE + Math.round(Mth.sin(ticks / FOCUS_PERIOD) * FOCUS_AMPLITUDE));
        return smoothPacked(level);
    }

    static int glow(float ticks) {
        float flicker = Mth.sin(ticks * GLOW_RATE) * Mth.cos(ticks * GLOW_DRIFT_RATE);
        int level = Math.min(MAX_LIGHT_LEVEL, GLOW_BASE + Math.round(flicker * GLOW_AMPLITUDE));
        return smoothPacked(level);
    }

    static int smoothPacked(int level) {
        int component = Mth.clamp(level, 0, MAX_LIGHT_LEVEL);
        return component | component << 16;
    }
}
