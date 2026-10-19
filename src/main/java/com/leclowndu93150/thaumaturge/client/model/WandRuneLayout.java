package com.leclowndu93150.thaumaturge.client.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;

final class WandRuneLayout {
    private static final float PIXEL = 1.0F / 16.0F;
    private static final int GLYPH_COUNT = 16;

    private static final int SCEPTRE_RUNE_COUNT = 10;
    private static final float SCEPTRE_SPIN_PER_TICK = 1.2F;
    private static final float SCEPTRE_RADIUS = 0.16F;
    private static final float SCEPTRE_Y = 5.6F * PIXEL;
    private static final float SCEPTRE_HALF_SIZE = 0.8F * PIXEL;
    private static final int SCEPTRE_GLYPH_STRIDE = 7;

    private static final int SIDE_COUNT = 4;
    private static final float SIDE_YAW_STEP = 90.0F;
    private static final int WAND_RUNES_PER_SIDE = 4;
    private static final int STAFF_RUNES_PER_SIDE = 8;
    private static final float WAND_RADIUS = 1.05F * PIXEL;
    private static final float STAFF_RADIUS = 1.3F * PIXEL;
    private static final float WAND_Y_START = 4.0F * PIXEL;
    private static final float STAFF_Y_START = 7.6F * PIXEL;
    private static final float WAND_Y_STEP = 2.8F * PIXEL;
    private static final float STAFF_Y_STEP = 3.1F * PIXEL;
    private static final float WAND_HALF_SIZE = 0.85F * PIXEL;
    private static final float STAFF_HALF_SIZE = 1.05F * PIXEL;
    private static final int SIDE_GLYPH_STEP = 5;
    private static final int SIDE_GLYPH_STRIDE = 3;

    private WandRuneLayout() {}

    static List<Rune> create(boolean sceptre, boolean sideRunes, boolean staff, float ticks) {
        List<Rune> runes = new ArrayList<>();
        if (sceptre) {
            for (int index = 0; index < SCEPTRE_RUNE_COUNT; index++) {
                runes.add(new Rune(
                        SCEPTRE_SPIN_PER_TICK * ticks + index * 360.0F / SCEPTRE_RUNE_COUNT,
                        SCEPTRE_RADIUS,
                        SCEPTRE_Y,
                        SCEPTRE_HALF_SIZE,
                        index * SCEPTRE_GLYPH_STRIDE % GLYPH_COUNT,
                        index));
            }
        }
        if (sideRunes) {
            int count = staff ? STAFF_RUNES_PER_SIDE : WAND_RUNES_PER_SIDE;
            float radius = staff ? STAFF_RADIUS : WAND_RADIUS;
            float yStart = staff ? STAFF_Y_START : WAND_Y_START;
            float yStep = staff ? STAFF_Y_STEP : WAND_Y_STEP;
            float halfSize = staff ? STAFF_HALF_SIZE : WAND_HALF_SIZE;
            for (int side = 0; side < SIDE_COUNT; side++) {
                for (int index = 0; index < count; index++) {
                    int glyph = (side * SIDE_GLYPH_STEP + index * SIDE_GLYPH_STRIDE) % GLYPH_COUNT;
                    int slot = SCEPTRE_RUNE_COUNT + side * count + index;
                    runes.add(new Rune(side * SIDE_YAW_STEP, radius, yStart + index * yStep, halfSize, glyph, slot));
                }
            }
        }
        return List.copyOf(runes);
    }

    static float pulse(float ticks, int slot) {
        return Mth.sin(ticks * 0.11F + slot * 2.39F);
    }

    static float swell(float ticks, int slot) {
        float phase = slot * 2.39F;
        return Mth.sin(ticks * 0.073F + phase * 2.39F);
    }

    record Rune(float yawDegrees, float radius, float y, float halfSize, int glyph, int slot) {}
}
