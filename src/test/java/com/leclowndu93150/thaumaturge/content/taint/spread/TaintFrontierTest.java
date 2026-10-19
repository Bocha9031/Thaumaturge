package com.leclowndu93150.thaumaturge.content.taint.spread;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TaintFrontierTest {
    @Test
    void fluxSaturationAcceleratesFrontierAtMostFiftyPercent() {
        assertEquals(50, TaintFrontier.oddsAgainst(10, 0.0F));
        assertEquals(40, TaintFrontier.oddsAgainst(10, 0.5F));
        assertEquals(33, TaintFrontier.oddsAgainst(10, 2.0F));
        assertEquals(33, TaintFrontier.oddsAgainst(10, 20.0F));
    }

    @Test
    void frontierPressureHasBoundedFluxBonus() {
        assertEquals(0.01F, TaintFrontier.pressureForSaturation(0.0F));
        assertEquals(0.02F, TaintFrontier.pressureForSaturation(1.0F));
        assertEquals(0.03F, TaintFrontier.pressureForSaturation(2.0F));
        assertEquals(0.03F, TaintFrontier.pressureForSaturation(20.0F));
    }
}
