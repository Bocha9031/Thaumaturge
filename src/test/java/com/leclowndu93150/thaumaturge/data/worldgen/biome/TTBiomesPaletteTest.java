package com.leclowndu93150.thaumaturge.data.worldgen.biome;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class TTBiomesPaletteTest {
    @Test
    void biomeColorsMatchTheCurrentPalette() {
        assertEquals(0x55FF81, TTBiomes.MAGICAL_FOREST_GRASS);
        assertEquals(0x66FFC5, TTBiomes.MAGICAL_FOREST_FOLIAGE);
        assertEquals(0x0077EE, TTBiomes.MAGICAL_FOREST_WATER);

        assertEquals(0x404840, TTBiomes.EERIE_GRASS);
        assertEquals(0x222299, TTBiomes.EERIE_SKY);
        assertEquals(0x2E535F, TTBiomes.EERIE_WATER);

        assertEquals(0x6D4189, TTBiomes.TAINTED_LANDS_GRASS);
        assertEquals(0x7C6D87, TTBiomes.TAINTED_LANDS_FOLIAGE);
        assertEquals(0x7C44FF, TTBiomes.TAINTED_LANDS_SKY);
        assertEquals(0x7D2CA7, TTBiomes.TAINTED_LANDS_WATER);
        assertEquals(0x2A0A3D, TTBiomes.TAINTED_LANDS_WATER_FOG);
    }
}
