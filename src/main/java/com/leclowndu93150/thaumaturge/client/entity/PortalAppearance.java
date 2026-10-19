package com.leclowndu93150.thaumaturge.client.entity;

import net.minecraft.util.Mth;

/** Computes the opening, pulse, and damage response for cultist portal visuals. */
final class PortalAppearance {
    private static final float OPENING_TICKS = 50.0F;
    private static final float VANILLA_HURT_TICKS = 10.0F;
    private static final float MIN_SLIT = 0.04F;
    private static final float CALM_RATE = 0.035F;
    private static final float AGITATED_RATE = 0.16F;
    private static final float WIDTH_RATE = 1.37F;
    private static final float WIDTH_PHASE = 1.1F;
    private static final float HEIGHT_BREATH = 0.16F;
    private static final float WIDTH_BREATH = 0.22F;
    private static final float DAMAGE_FADE = 0.62F;
    private static final float FLICKER_RATE = 0.9F;
    private static final float FLICKER_DEPTH = 0.08F;
    private static final float MIN_ALPHA = 0.2F;
    private static final float FLINCH_HEIGHT = 0.14F;
    private static final float FLINCH_WIDTH = 0.1F;
    private static final float PULSE_SCALE = 0.28F;
    private static final float HALF = 0.5F;

    private PortalAppearance() {}

    static float openWidth(float activeTicks) {
        float opening = Mth.clamp(activeTicks / OPENING_TICKS, 0.0F, 1.0F);
        float eased = opening * opening * (3.0F - 2.0F * opening);
        return Mth.lerp(eased, MIN_SLIT, 1.0F);
    }

    static float halfHeight(float height, float damage, float time, float flinch, float beat) {
        float breath = damage * HEIGHT_BREATH * breathing(damage, time, 1.0F, 0.0F);
        float pulse = swell(beat);
        return height * HALF * (1.0F - breath) * (1.0F - FLINCH_HEIGHT * flinch) * (1.0F + PULSE_SCALE * pulse);
    }

    static float halfWidth(float width, float damage, float time, float flinch, float beat) {
        float breath = damage * WIDTH_BREATH * breathing(damage, time, WIDTH_RATE, WIDTH_PHASE);
        float pulse = swell(beat);
        return width * HALF * (1.0F - breath) * (1.0F + FLINCH_WIDTH * flinch) * (1.0F + PULSE_SCALE * pulse);
    }

    static float opacity(float damage, float time) {
        float fade = damage * DAMAGE_FADE * breathing(damage, time, 1.0F, 0.0F);
        float flicker = damage * FLICKER_DEPTH * trough(time * FLICKER_RATE);
        return Mth.clamp(1.0F - fade - flicker, MIN_ALPHA, 1.0F);
    }

    static float damage(float healthFraction) {
        return Mth.clamp(1.0F - healthFraction, 0.0F, 1.0F);
    }

    static float flinch(int hurtTime, float partialTicks) {
        return Mth.clamp((hurtTime - partialTicks) / VANILLA_HURT_TICKS, 0.0F, 1.0F);
    }

    static float beat(int pulseTicks, float partialTicks, int pulseDuration) {
        return pulseTicks <= 0 ? 0.0F : Mth.clamp(1.0F - (pulseTicks - partialTicks) / pulseDuration, 0.0F, 1.0F);
    }

    private static float breathing(float damage, float time, float rate, float phase) {
        float calm = trough(time * CALM_RATE * rate + phase);
        float agitated = trough(time * AGITATED_RATE * rate + phase);
        return Mth.lerp(damage, calm, agitated);
    }

    private static float trough(float angle) {
        return HALF + HALF * Mth.sin(angle);
    }

    private static float swell(float beat) {
        return beat <= 0.0F ? 0.0F : Mth.sin(beat * Mth.PI);
    }
}
