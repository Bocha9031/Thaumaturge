package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NodeHungerMawTest {
    @Test
    void usesTheCurrentMawRadiusAndIncludesItsBoundary() {
        assertFalse(NodeHunger.shouldSwallow(1.5001));
        assertTrue(NodeHunger.shouldSwallow(1.5));
        assertTrue(NodeHunger.shouldSwallow(1.45));
    }
}
