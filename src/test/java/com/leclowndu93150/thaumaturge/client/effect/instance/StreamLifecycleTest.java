package com.leclowndu93150.thaumaturge.client.effect.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StreamLifecycleTest {
    @Test
    void expiresAtAgeLimitRatherThanOnTheFollowingTick() {
        assertFalse(StreamInstance.expired(9, 10, 1, false));
        assertTrue(StreamInstance.expired(10, 10, 1, false));
    }

    @Test
    void expiresImmediatelyWhenTrailRunsOutOrTargetDisappears() {
        assertTrue(StreamInstance.expired(0, 10, 0, false));
        assertTrue(StreamInstance.expired(0, 10, 1, true));
    }

    @Test
    void tailTaperNeverReversesTheFinalStreamRadii() {
        assertEquals(1.0, StreamInstance.tailTaper(10, 20), 1.0E-9);
        assertEquals(0.0, StreamInstance.tailTaper(18, 20), 1.0E-9);
        assertEquals(0.0, StreamInstance.tailTaper(19, 20), 1.0E-9);
    }
}
