package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

class ProvideBehaviorPortTest {
    @Test
    void invalidTaskLegFallsBackToCollect() {
        assertEquals(0, ProvideBehavior.legOf(-1));
        assertEquals(0, ProvideBehavior.legOf(3));
    }

    @Test
    void validTaskLegIsPreserved() {
        assertEquals(0, ProvideBehavior.legOf(0));
        assertEquals(1, ProvideBehavior.legOf(1));
        assertEquals(2, ProvideBehavior.legOf(2));
    }

    @Test
    void deliveryTargetRequiresAnEntityOrACompleteBlockFacePair() {
        assertFalse(ProvideBehavior.hasDeliveryTarget(null, null, null));
        assertFalse(ProvideBehavior.hasDeliveryTarget(null, BlockPos.ZERO, null));
        assertFalse(ProvideBehavior.hasDeliveryTarget(null, null, Direction.NORTH));
        assertTrue(ProvideBehavior.hasDeliveryTarget(null, BlockPos.ZERO, Direction.NORTH));
    }
}
