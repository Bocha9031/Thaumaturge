package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import org.junit.jupiter.api.Test;

class NodeAnimationTest {
    @Test
    void shellEnvelopesStayFiniteForLargeAspectListsAndLongRunningWorlds() {
        for (int shell = 0; shell < 64; shell++) {
            for (float ticks : new float[] {-40.5F, 0.0F, 14.0F, 142.3F, 1000000.0F}) {
                for (int amount : new int[] {-1, 0, 10, 50, Integer.MAX_VALUE}) {
                    float radius = NodeAnimation.shellRadius(NodeAnimation.charge(amount), ticks, shell);
                    assertTrue(Float.isFinite(radius));
                    assertTrue(radius >= 0.20F && radius <= 0.97F);
                    assertTrue(Float.isFinite(NodeAnimation.shellAngle(ticks, shell)));
                }
            }
        }
    }

    @Test
    void brightnessModifiersAndDistanceFadeStayBounded() {
        assertEquals(0.5F, NodeAnimation.opacity(0.5F, null, 0.0F));
        assertTrue(NodeAnimation.opacity(0.5F, NodeModifier.BRIGHT, 0.0F) > 0.5F);
        assertTrue(NodeAnimation.opacity(0.5F, NodeModifier.PALE, 0.0F) < 0.5F);
        float lowestFade = 1.0F;
        float highestFade = 0.0F;
        for (int tick = 0; tick < 100; tick++) {
            float fading = NodeAnimation.opacity(1.0F, NodeModifier.FADING, tick);
            lowestFade = Math.min(lowestFade, fading);
            highestFade = Math.max(highestFade, fading);
            for (NodeModifier modifier : NodeModifier.values()) {
                assertEquals(0.0F, NodeAnimation.opacity(-1.0F, modifier, tick));
                float alpha = NodeAnimation.opacity(2.0F, modifier, tick);
                assertTrue(alpha >= 0.0F && alpha <= 1.0F);
            }
        }
        assertTrue(highestFade - lowestFade > 0.3F);
    }
}
