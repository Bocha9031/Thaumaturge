package com.leclowndu93150.thaumaturge.client.effect.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BeamArcTextureCoordinatesTest {
    @Test
    void zeroLengthArcKeepsTextureCoordinatesFinite() {
        assertEquals(0.0F, BeamManager.arcTextureU(0, 0.0F));
        assertEquals(0.0F, BeamManager.arcTextureU(1, 0.0F));
    }

    @Test
    void arcTextureCoordinatesScaleByChordLength() {
        assertEquals(0.0F, BeamManager.arcTextureU(0, 2.0F));
        assertEquals(0.5F, BeamManager.arcTextureU(1, 2.0F));
        assertEquals(1.0F, BeamManager.arcTextureU(2, 2.0F));
    }
}
