package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import org.junit.jupiter.api.Test;

class NodeHungerRangeTest {
    @Test
    void intermediateQualityRangesUseWholeStepFractions() {
        assertEquals(11, NodeHunger.scaledEatRange(10, 15, NodeModifier.PALE));
        assertEquals(13, NodeHunger.scaledEatRange(10, 15, null));
    }

    @Test
    void endpointsAndReversedBoundsRemainStable() {
        assertEquals(15, NodeHunger.scaledEatRange(10, 15, NodeModifier.BRIGHT));
        assertEquals(10, NodeHunger.scaledEatRange(10, 15, NodeModifier.FADING));
        assertEquals(10, NodeHunger.scaledEatRange(10, 5, NodeModifier.BRIGHT));
    }
}
