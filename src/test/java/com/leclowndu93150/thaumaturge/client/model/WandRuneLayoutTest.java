package com.leclowndu93150.thaumaturge.client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class WandRuneLayoutTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void sceptreUsesTenRotatingRunesAroundItsHead() {
        List<WandRuneLayout.Rune> runes = WandRuneLayout.create(true, false, false, 0.0F);

        assertEquals(10, runes.size());
        for (int index = 0; index < runes.size(); index++) {
            WandRuneLayout.Rune rune = runes.get(index);
            assertEquals(index * 36.0F, rune.yawDegrees(), EPSILON);
            assertEquals(0.16F, rune.radius(), EPSILON);
            assertEquals(5.6F / 16.0F, rune.y(), EPSILON);
            assertEquals(index * 7 % 16, rune.glyph());
        }

        List<WandRuneLayout.Rune> rotated = WandRuneLayout.create(true, false, false, 10.0F);
        assertEquals(12.0F, rotated.getFirst().yawDegrees(), EPSILON);
    }

    @Test
    void ordinaryWandHasFourGlyphsOnEachOfItsFourSides() {
        List<WandRuneLayout.Rune> runes = WandRuneLayout.create(false, true, false, 0.0F);

        assertEquals(16, runes.size());
        for (int side = 0; side < 4; side++) {
            for (int index = 0; index < 4; index++) {
                WandRuneLayout.Rune rune = runes.get(side * 4 + index);
                assertEquals(side * 90.0F, rune.yawDegrees(), EPSILON);
                assertEquals(1.05F / 16.0F, rune.radius(), EPSILON);
                assertEquals((4.0F + index * 2.8F) / 16.0F, rune.y(), EPSILON);
                assertTrue(rune.glyph() >= 0 && rune.glyph() < 16);
            }
        }
    }

    @Test
    void staffHasEightGlyphsOnEachOfItsFourSides() {
        List<WandRuneLayout.Rune> runes = WandRuneLayout.create(false, true, true, 0.0F);

        assertEquals(32, runes.size());
        for (int side = 0; side < 4; side++) {
            for (int index = 0; index < 8; index++) {
                WandRuneLayout.Rune rune = runes.get(side * 8 + index);
                assertEquals(side * 90.0F, rune.yawDegrees(), EPSILON);
                assertEquals(1.3F / 16.0F, rune.radius(), EPSILON);
                assertEquals((7.6F + index * 3.1F) / 16.0F, rune.y(), EPSILON);
                assertTrue(rune.glyph() >= 0 && rune.glyph() < 16);
            }
        }
    }

    @Test
    void noRuneOptionsProduceNoGeometry() {
        assertTrue(WandRuneLayout.create(false, false, false, 0.0F).isEmpty());
    }
}
