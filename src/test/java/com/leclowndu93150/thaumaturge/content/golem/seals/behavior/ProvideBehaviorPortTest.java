package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

class ProvideBehaviorPortTest {
    @Test
    void deliveryTargetRequiresAnEntityOrACompleteBlockFacePair() {
        assertFalse(ProvideBehavior.hasDeliveryTarget(null, null, null));
        assertFalse(ProvideBehavior.hasDeliveryTarget(null, BlockPos.ZERO, null));
        assertFalse(ProvideBehavior.hasDeliveryTarget(null, null, Direction.NORTH));
        assertTrue(ProvideBehavior.hasDeliveryTarget(null, BlockPos.ZERO, Direction.NORTH));
    }
}
