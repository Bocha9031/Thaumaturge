package com.leclowndu93150.thaumaturge.content.entity.construct;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ArcaneBoreRailLogicTest {
    @Test
    void activatorRailAndPassengerControlDoNotFallBackToBlockSignal() {
        assertFalse(EntityArcaneBore.shouldFollowBlockSignal(true, false));
        assertFalse(EntityArcaneBore.shouldFollowBlockSignal(false, true));
    }

    @Test
    void ordinaryUnmountedBoreCanUseBlockSignal() {
        assertTrue(EntityArcaneBore.shouldFollowBlockSignal(false, false));
    }
}
