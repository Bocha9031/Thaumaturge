package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.util.Mth;

final class DeconstructionTableRenderMath {
    private static final float ITEM_BOB_HEIGHT = 0.045F;
    private static final float ITEM_BOB_PERIOD = 90.0F;
    private static final float GLOW_ALPHA = 0.28F;
    private static final float GLOW_ALPHA_SWING = 0.08F;
    private static final float GLOW_PULSE_PERIOD = 60.0F;

    private DeconstructionTableRenderMath() {}

    static float itemBob(float ticks) {
        return Mth.sin(ticks * Mth.TWO_PI / ITEM_BOB_PERIOD) * ITEM_BOB_HEIGHT;
    }

    static float glowAlpha(float ticks) {
        return GLOW_ALPHA + Mth.sin(ticks * Mth.TWO_PI / GLOW_PULSE_PERIOD) * GLOW_ALPHA_SWING;
    }

    static float aspectSpin(float ticks) {
        return -ticks;
    }
}
