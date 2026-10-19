package com.leclowndu93150.thaumaturge.compat.jade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JadeNodeVisibilityTest {
    @Test
    void offHidesNodesRegardlessOfRevealGear() {
        assertFalse(JadeConfig.shouldShowNodes(JadeConfig.DisplayMode.OFF, true, true));
    }

    @Test
    void onIgnoresTheGogglesGate() {
        assertTrue(JadeConfig.shouldShowNodes(JadeConfig.DisplayMode.ON, false, false));
    }

    @Test
    void gogglesModeAcceptsEitherRevealingGearOrThaumometer() {
        assertTrue(JadeConfig.shouldShowNodes(JadeConfig.DisplayMode.GOGGLES, true, false));
        assertTrue(JadeConfig.shouldShowNodes(JadeConfig.DisplayMode.GOGGLES, false, true));
        assertFalse(JadeConfig.shouldShowNodes(JadeConfig.DisplayMode.GOGGLES, false, false));
    }
}
