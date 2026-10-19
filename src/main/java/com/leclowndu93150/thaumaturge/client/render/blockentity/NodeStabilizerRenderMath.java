package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeStabilizer;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeTransducer;
import net.minecraft.util.FastColor.ARGB32;

final class NodeStabilizerRenderMath {
    static final float ARM_SLIDE = 0.37F;

    private static final float TRANSDUCER_GLOW_FLOOR = 0.25F;
    private static final float TRANSDUCER_GLOW_RANGE = 1.0F - TRANSDUCER_GLOW_FLOOR;
    private static final float GLOW_BASE = 0.78F;
    private static final float GLOW_AMPLITUDE = 0.22F;
    private static final float GLOW_PERIOD = 44.0F;
    private static final float GLOW_ARM_PHASE = 0.6F;
    private static final float BUBBLE_OPACITY = 0.3F;
    private static final float BUBBLE_PULSE_BASE = 0.7F;
    private static final float BUBBLE_PERIOD = 90.0F;

    private NodeStabilizerRenderMath() {}

    static float engagement(int count) {
        return Math.clamp((float) count / BlockEntityNodeStabilizer.MAX_COUNT, 0.0F, 1.0F);
    }

    static float transducerExtension(float count) {
        return Math.clamp(count / BlockEntityNodeTransducer.CHARGE_TARGET, 0.0F, 1.0F);
    }

    static float transducerGlow(float extension) {
        return TRANSDUCER_GLOW_FLOOR + Math.clamp(extension, 0.0F, 1.0F) * TRANSDUCER_GLOW_RANGE;
    }

    static int glowColor(int tint, float glow, float ticks, int arm) {
        float pulse = GLOW_BASE
                + GLOW_AMPLITUDE * (float) Math.sin(Math.PI * 2.0 * ticks / GLOW_PERIOD + arm * GLOW_ARM_PHASE);
        int alpha = Math.round(Math.clamp(glow * pulse, 0.0F, 1.0F) * 255.0F);
        return ARGB32.color(alpha, tint);
    }

    static float bubbleAlpha(int count, float ticks) {
        float breath = 0.5F + 0.5F * (float) Math.sin(Math.PI * 2.0 * ticks / BUBBLE_PERIOD);
        float pulse = BUBBLE_PULSE_BASE + breath * (1.0F - BUBBLE_PULSE_BASE);
        return engagement(count) * BUBBLE_OPACITY * pulse;
    }
}
