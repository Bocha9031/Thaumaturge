package com.leclowndu93150.thaumaturge.client.render.research;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.util.FastColor.ABGR32;
import org.junit.jupiter.api.Test;

class ResearchIconTexturesTest {
    @Test
    void removesOnlyConnectedDarkBackgroundAndPreservesEnclosedDetails() {
        int background = ABGR32.color(255, 10, 11, 14);
        int purple = ABGR32.color(255, 190, 50, 120);
        try (NativeImage image = new NativeImage(5, 5, true)) {
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) image.setPixelRGBA(x, y, background);
            }
            for (int y = 1; y < 4; y++) {
                for (int x = 1; x < 4; x++) image.setPixelRGBA(x, y, purple);
            }
            image.setPixelRGBA(2, 2, background);

            ResearchIconTextures.clearOpaqueBackground(image);

            assertEquals(background & 0x00FFFFFF, image.getPixelRGBA(0, 0));
            assertEquals(purple, image.getPixelRGBA(1, 1));
            assertEquals(background, image.getPixelRGBA(2, 2));
        }
    }

    @Test
    void resourcePackTransparencyIsNotChanged() {
        try (NativeImage image = new NativeImage(2, 2, true)) {
            int black = ABGR32.color(255, 0, 0, 0);
            int translucent = ABGR32.color(128, 10, 20, 30);
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 2; x++) image.setPixelRGBA(x, y, black);
            }
            image.setPixelRGBA(1, 1, translucent);

            ResearchIconTextures.clearOpaqueBackground(image);

            assertEquals(black, image.getPixelRGBA(0, 0));
            assertEquals(translucent, image.getPixelRGBA(1, 1));
        }
    }

    @Test
    void allAffectedAssetsLoseTheirBackdropWithoutChangingAnyRgbColors() throws Exception {
        Path root = Path.of(
                System.getProperty("thaumaturge.testResourceRoot"), "assets", "thaumaturge", "textures", "research");
        for (String icon : new String[] {"r_researcher1", "r_researcher2", "r_runicupg"}) {
            try (InputStream stream = Files.newInputStream(root.resolve(icon + ".png"));
                    NativeImage image = NativeImage.read(stream)) {
                int[] before = new int[image.getWidth() * image.getHeight()];
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++)
                        before[y * image.getWidth() + x] = image.getPixelRGBA(x, y);
                }
                ResearchIconTextures.clearOpaqueBackground(image);
                int cleared = 0;
                int opaque = 0;
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        int original = before[y * image.getWidth() + x];
                        int actual = image.getPixelRGBA(x, y);
                        assertEquals(original & 0x00FFFFFF, actual & 0x00FFFFFF, icon);
                        if (ABGR32.alpha(actual) == 0) cleared++;
                        else {
                            assertEquals(original, actual, icon);
                            opaque++;
                        }
                    }
                }
                assertTrue(cleared > 0, icon);
                assertTrue(opaque > 0, icon);
            }
        }
    }
}
