package com.leclowndu93150.thaumaturge.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class FluxScrubberModelDataTest {
    @Test
    void generatedWrappersKeepTheCustomMeshAndBlockOnlyTipVisibility() throws Exception {
        TTModelProvider provider = new TTModelProvider(mock(PackOutput.class));
        Map<ResourceLocation, Supplier<JsonElement>> models = new HashMap<>();
        setField(provider, "modelOutput", (BiConsumer<ResourceLocation, Supplier<JsonElement>>) models::put);

        Method register = TTModelProvider.class.getDeclaredMethod("registerFluxScrubberModels");
        register.setAccessible(true);
        register.invoke(provider);

        JsonObject block = model(models, "block/flux_scrubber");
        JsonObject item = model(models, "item/flux_scrubber");
        for (JsonObject wrapper : new JsonObject[] {block, item}) {
            assertEquals("thaumaturge:mesh", wrapper.get("loader").getAsString());
            assertEquals(
                    "thaumaturge:models/mesh/flux_scrubber.ttmesh",
                    wrapper.get("model").getAsString());
            assertEquals("minecraft:cutout", wrapper.get("render_type").getAsString());
            assertEquals(
                    "thaumaturge:block/flux_scrubber",
                    wrapper.getAsJsonObject("textures").get("texture0").getAsString());
        }
        assertFalse(block.getAsJsonObject("visibility").get("Tip").getAsBoolean());
        assertNotNull(item.get("transform"));
        assertEquals("corner", item.getAsJsonObject("transform").get("origin").getAsString());
        assertEquals(
                "-0.5",
                item.getAsJsonObject("transform")
                        .getAsJsonArray("translation")
                        .get(2)
                        .getAsString());
    }

    private static JsonObject model(Map<ResourceLocation, Supplier<JsonElement>> models, String path) {
        return models.get(ResourceLocation.fromNamespaceAndPath("thaumaturge", path))
                .get()
                .getAsJsonObject();
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = TTModelProvider.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
