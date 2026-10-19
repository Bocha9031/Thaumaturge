package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeStabilizer;
import org.junit.jupiter.api.Test;

class NodeStabilizerRenderMathTest {
    @Test
    void stabilizerEngagementIsBoundedByItsStoredCount() {
        assertEquals(0.0F, NodeStabilizerRenderMath.engagement(-1));
        assertEquals(0.0F, NodeStabilizerRenderMath.engagement(0));
        assertEquals(1.0F, NodeStabilizerRenderMath.engagement(BlockEntityNodeStabilizer.MAX_COUNT));
        assertEquals(1.0F, NodeStabilizerRenderMath.engagement(BlockEntityNodeStabilizer.MAX_COUNT + 1));
    }

    @Test
    void transducerExtensionAndGlowAreClampedAndHaveAnIdleGlowFloor() {
        assertEquals(0.0F, NodeStabilizerRenderMath.transducerExtension(-1.0F));
        assertEquals(0.5F, NodeStabilizerRenderMath.transducerExtension(500.0F));
        assertEquals(1.0F, NodeStabilizerRenderMath.transducerExtension(1000.0F));
        assertEquals(0.25F, NodeStabilizerRenderMath.transducerGlow(0.0F));
        assertEquals(0.625F, NodeStabilizerRenderMath.transducerGlow(0.5F));
        assertEquals(1.0F, NodeStabilizerRenderMath.transducerGlow(1.0F));
    }

    @Test
    void visualPulsesStayWithinTheirIntendedAlphaRange() {
        assertEquals(0.0F, NodeStabilizerRenderMath.bubbleAlpha(0, 0.0F));
        assertEquals(0.255F, NodeStabilizerRenderMath.bubbleAlpha(BlockEntityNodeStabilizer.MAX_COUNT, 0.0F), 1.0E-6F);
        assertEquals(0.3F, NodeStabilizerRenderMath.bubbleAlpha(BlockEntityNodeStabilizer.MAX_COUNT, 22.5F), 1.0E-6F);
        int atPeak = NodeStabilizerRenderMath.glowColor(0xFFFFFF, 1.0F, 11.0F, 0);
        int atTrough = NodeStabilizerRenderMath.glowColor(0xFFFFFF, 1.0F, 33.0F, 0);
        assertEquals(255, atPeak >>> 24);
        assertEquals(143, atTrough >>> 24);
    }
}
