package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.FastColor.ABGR32;
import org.junit.jupiter.api.Test;

class NodeTextureTest {
    @Test
    void glowGradientsAttenuateRgbWithoutLosingAlphaForEveryGlowStrip() {
        try (NativeImage image = new NativeImage(64, 64, true)) {
            int[] strips = {0, 6, 7, 8, 9, 25};
            for (int strip : strips) {
                image.setPixelRGBA(0, strip * 2, ABGR32.color(128, 255, 255, 255));
                image.setPixelRGBA(63, strip * 2 + 1, ABGR32.color(8, 255, 255, 255));
            }
            image.setPixelRGBA(1, 0, ABGR32.color(0, 255, 255, 255));
            image.setPixelRGBA(2, 0, ABGR32.color(255, 25, 110, 240));
            image.setPixelRGBA(3, 0, ABGR32.color(128, 50, 100, 200));

            NodeTexture.premultiplyGlowStrips(image);

            for (int strip : strips) {
                assertEquals(ABGR32.color(128, 128, 128, 128), image.getPixelRGBA(0, strip * 2));
                assertEquals(ABGR32.color(8, 8, 8, 8), image.getPixelRGBA(63, strip * 2 + 1));
            }
            assertEquals(0, image.getPixelRGBA(1, 0));
            assertEquals(ABGR32.color(255, 25, 110, 240), image.getPixelRGBA(2, 0));
            assertEquals(ABGR32.color(128, 25, 50, 100), image.getPixelRGBA(3, 0));
        }
    }

    @Test
    void coloredCoreStripsKeepTheirOriginalColorAndOpacity() {
        try (NativeImage image = new NativeImage(32, 32, true)) {
            int color = ABGR32.color(64, 25, 110, 240);
            for (int strip = 1; strip <= 5; strip++) {
                image.setPixelRGBA(0, strip, color);
            }

            NodeTexture.premultiplyGlowStrips(image);

            for (int strip = 1; strip <= 5; strip++) {
                assertEquals(color, image.getPixelRGBA(0, strip));
            }
        }
    }
}
