package com.leclowndu93150.thaumaturge.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class EtherealBloomTextureParityTest {
    private static final String TEXTURE_PATH = "/assets/thaumaturge/textures/block/ethereal_bloom.png";
    private static final String SOURCE_SHA_256 = "fe1568fecc7b28ad1b6d6543a9a75fc7b0505cbcc42f3888ffb7146cc8a3641f";

    @Test
    void currentEtherealBloomTextureIsPresentAndReadable() throws Exception {
        byte[] texture = readTexture();
        assertEquals(SOURCE_SHA_256, sha256(texture));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(texture));
        assertNotNull(image);
        assertEquals(16, image.getWidth());
        assertEquals(16, image.getHeight());
    }

    @Test
    void generatedBlockAndItemModelsUseTheEtherealBloomTexture() throws Exception {
        TTModelProvider provider = new TTModelProvider(mock(PackOutput.class));
        Map<ResourceLocation, Supplier<JsonElement>> generatedModels = new HashMap<>();
        setField(provider, "blockStateOutput", (Consumer<BlockStateGenerator>) generator -> {});
        setField(provider, "modelOutput", (BiConsumer<ResourceLocation, Supplier<JsonElement>>) generatedModels::put);

        Method plantModels = TTModelProvider.class.getDeclaredMethod("plantModels");
        plantModels.setAccessible(true);
        plantModels.invoke(provider);

        Supplier<JsonElement> blockModelSupplier =
                generatedModels.get(ModelLocationUtils.getModelLocation(TTBlocks.ETHEREAL_BLOOM.get()));
        Supplier<JsonElement> itemModelSupplier =
                generatedModels.get(ModelLocationUtils.getModelLocation(TTItems.ETHEREAL_BLOOM.get()));
        assertNotNull(blockModelSupplier);
        assertNotNull(itemModelSupplier);
        JsonObject blockModel = blockModelSupplier.get().getAsJsonObject();
        JsonObject itemModel = itemModelSupplier.get().getAsJsonObject();
        ResourceLocation bloomTexture = TextureMapping.getBlockTexture(TTBlocks.ETHEREAL_BLOOM.get());

        assertEquals(
                bloomTexture.toString(),
                blockModel.getAsJsonObject("textures").get("cross").getAsString());
        assertEquals(
                bloomTexture.toString(),
                itemModel.getAsJsonObject("textures").get("layer0").getAsString());
    }

    private static byte[] readTexture() throws IOException {
        try (InputStream input = EtherealBloomTextureParityTest.class.getResourceAsStream(TEXTURE_PATH)) {
            assertNotNull(input, "Expected Ethereal Bloom texture resource " + TEXTURE_PATH);
            return input.readAllBytes();
        }
    }

    private static String sha256(byte[] bytes) throws Exception {
        byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes);
        return java.util.HexFormat.of().formatHex(digest);
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = TTModelProvider.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
