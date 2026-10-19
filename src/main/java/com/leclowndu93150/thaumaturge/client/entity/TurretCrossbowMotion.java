package com.leclowndu93150.thaumaturge.client.entity;

import net.minecraft.util.Mth;

final class TurretCrossbowMotion {
    private TurretCrossbowMotion() {}

    static float snap(float progress) {
        return progress <= 0.0F ? 0.0F : Mth.sin(Mth.PI * Mth.sqrt(Math.min(progress, 1.0F)));
    }

    static float hurtFade(int hurtTime, float partialTick, int hurtDuration) {
        return hurtDuration > 0 ? Mth.clamp((hurtTime - partialTick) / hurtDuration, 0.0F, 1.0F) : 0.0F;
    }
}
