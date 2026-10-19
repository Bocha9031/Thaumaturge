package com.leclowndu93150.thaumaturge.client.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

final class CultistClericRenderMath {
    private static final float HOVER_HEIGHT = 0.2F;
    private static final float BOB_AMPLITUDE = 0.11F;
    private static final float BOB_PERIOD_TICKS = 70.0F;
    private static final float PHASE_SPREAD = 0.618034F;
    private static final float HEAD_CLEARANCE = 0.2F;
    private static final float DRAW_OUT_TICKS = 10.0F;

    private CultistClericRenderMath() {}

    static float bob(float age, int entityId) {
        float phase = Mth.frac(entityId * PHASE_SPREAD) * BOB_PERIOD_TICKS;
        return HOVER_HEIGHT + BOB_AMPLITUDE * Mth.sin((age + phase) / BOB_PERIOD_TICKS * Mth.TWO_PI);
    }

    static float lineStartY(float entityHeight, float bob) {
        return entityHeight + HEAD_CLEARANCE + bob;
    }

    static Vec3 fromFocusToHead(Vec3 entityPosition, Vec3 focus, float lineStartY) {
        return new Vec3(
                entityPosition.x - focus.x, entityPosition.y + lineStartY - focus.y, entityPosition.z - focus.z);
    }

    static float fade(float age) {
        return Mth.clamp(age / DRAW_OUT_TICKS, 0.0F, 1.0F);
    }
}
