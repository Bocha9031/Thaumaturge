package com.leclowndu93150.thaumaturge.client.effect.instance;

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
}
