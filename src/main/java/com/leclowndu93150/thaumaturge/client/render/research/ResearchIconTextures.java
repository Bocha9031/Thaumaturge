package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor.ABGR32;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ResearchIconTextures {
    private static final int BACKGROUND_LIMIT = 24;

    private ResearchIconTextures() {}

    @SubscribeEvent
    static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) ResearchIconTextures::reload);
    }

    private static void reload(ResourceManager resources) {
        for (String icon : new String[] {"r_researcher1", "r_researcher2", "r_runicupg"}) {
            ResourceLocation texture = TTIds.rl("textures/research/" + icon + ".png");
            try (InputStream stream = resources.open(texture)) {
                NativeImage image = NativeImage.read(stream);
                try {
                    clearOpaqueBackground(image);
                    Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(image));
                } catch (RuntimeException | Error e) {
                    image.close();
                    throw e;
                }
            } catch (IOException e) {
                Thaumaturge.LOGGER.error("Could not prepare research icon {}", texture, e);
            }
        }
    }

    static void clearOpaqueBackground(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Resource-pack images with authored transparency must be used unchanged.
                if (ABGR32.alpha(image.getPixelRGBA(x, y)) != 255) return;
            }
        }
        int[] queue = new int[width * height];
        int tail = 0;
        for (int x = 0; x < width; x++) {
            tail = clear(image, x, 0, queue, tail);
            tail = clear(image, x, height - 1, queue, tail);
        }
        for (int y = 0; y < height; y++) {
            tail = clear(image, 0, y, queue, tail);
            tail = clear(image, width - 1, y, queue, tail);
        }
        for (int head = 0; head < tail; head++) {
            int x = queue[head] % width;
            int y = queue[head] / width;
            tail = clear(image, x - 1, y, queue, tail);
            tail = clear(image, x + 1, y, queue, tail);
            tail = clear(image, x, y - 1, queue, tail);
            tail = clear(image, x, y + 1, queue, tail);
        }
    }

    private static int clear(NativeImage image, int x, int y, int[] queue, int tail) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return tail;
        int pixel = image.getPixelRGBA(x, y);
        if (ABGR32.alpha(pixel) != 255
                || Math.max(ABGR32.red(pixel), Math.max(ABGR32.green(pixel), ABGR32.blue(pixel))) > BACKGROUND_LIMIT) {
            return tail;
        }
        image.setPixelRGBA(x, y, pixel & 0x00FFFFFF);
        queue[tail++] = y * image.getWidth() + x;
        return tail;
    }
}
