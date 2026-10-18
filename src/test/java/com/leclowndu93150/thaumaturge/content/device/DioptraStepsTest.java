package com.leclowndu93150.thaumaturge.content.device;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DioptraStepsTest {
    @Test
    void readingsRoundToTheNearestStepAndStayWithinTheDisplayRange() {
        assertEquals(0, BlockEntityDioptra.toStep(-10.0F));
        assertEquals(1, BlockEntityDioptra.toStep(500.0F / 128.0F));
        assertEquals(BlockEntityDioptra.STEPS, BlockEntityDioptra.toStep(500.0F));
        assertEquals(BlockEntityDioptra.STEPS, BlockEntityDioptra.toStep(900.0F));
    }
}
