package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.util.Mth;

final class JarBrainRenderMath {
    static final float BOB_PERIOD = 76.0F;
    static final float BOB_AMPLITUDE = 0.022F;
    static final float XP_PERIOD = 5.0F;
    static final long CLOCK_WRAP = 72000L;

    private JarBrainRenderMath() {}

    static float age(long gameTime, float partialTick) {
        return Math.floorMod(gameTime, CLOCK_WRAP) + partialTick;
    }

    static float bobOffset(long gameTime, float partialTick, long blockSeed) {
        float animationAge = age(gameTime, partialTick);
        long phase = Math.floorMod(blockSeed, (long) BOB_PERIOD);
        return BOB_AMPLITUDE * Mth.sin(Mth.TWO_PI * (animationAge + phase) / BOB_PERIOD);
    }

    static float xpResponse(float animationAge, int xp, int maxXp) {
        float fill = Mth.clamp((float) xp / maxXp, 0.0F, 1.0F);
        return fill * Mth.sin(animationAge / XP_PERIOD);
    }

    static int visibleXp(int xp, int maxXp) {
        return Mth.clamp(xp, 0, Math.max(0, maxXp));
    }

    static float interpolateYaw(float previous, float current, float partialTick) {
        float delta = current - previous;
        delta -= Mth.TWO_PI * Mth.floor((delta + Mth.PI) / Mth.TWO_PI);
        return previous + delta * partialTick;
    }
}
