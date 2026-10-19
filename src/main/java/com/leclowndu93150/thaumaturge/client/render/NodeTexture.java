package com.leclowndu93150.thaumaturge.client.render;

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
public final class NodeTexture {
    public static final ResourceLocation TEXTURE = TTIds.rl("textures/misc/auranodes.png");
    private static final int GRID = 32;

    private NodeTexture() {}

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) NodeTexture::reload);
    }

    private static void reload(ResourceManager resources) {
        try (InputStream stream = resources.open(TEXTURE)) {
            NativeImage image = NativeImage.read(stream);
            try {
                premultiplyGlowStrips(image);
                Minecraft.getInstance().getTextureManager().register(TEXTURE, new DynamicTexture(image));
                Thaumaturge.LOGGER.debug("Prepared premultiplied node glow strips from {}", TEXTURE);
            } catch (RuntimeException | Error e) {
                image.close();
                throw e;
            }
        } catch (IOException e) {
            Thaumaturge.LOGGER.error("Could not prepare node glow texture {}", TEXTURE, e);
        }
    }

    static void premultiplyGlowStrips(NativeImage image) {
        int stripHeight = image.getHeight() / GRID;
        // ONE + ONE blending needs premultiplied glow RGB. Keep colored core strips as authored.
        // The last strip is the wisp glow, which uses the same atlas and additive blend.
        for (int strip : new int[] {0, 6, 7, 8, 9, 25}) {
            for (int y = strip * stripHeight; y < (strip + 1) * stripHeight; y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int color = image.getPixelRGBA(x, y);
                    int alpha = ABGR32.alpha(color);
                    image.setPixelRGBA(
                            x,
                            y,
                            ABGR32.color(
                                    alpha,
                                    ABGR32.blue(color) * alpha / 255,
                                    ABGR32.green(color) * alpha / 255,
                                    ABGR32.red(color) * alpha / 255));
                }
            }
        }
    }
}
