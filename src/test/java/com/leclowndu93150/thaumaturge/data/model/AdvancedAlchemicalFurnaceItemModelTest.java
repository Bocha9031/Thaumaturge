package com.leclowndu93150.thaumaturge.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class AdvancedAlchemicalFurnaceItemModelTest {
    @Test
    void generatedItemModelRetainsEntityDisplayTransforms() throws Exception {
        TTModelProvider provider = new TTModelProvider(mock(PackOutput.class));
        Map<ResourceLocation, Supplier<JsonElement>> models = new HashMap<>();
        setField(provider, "modelOutput", (BiConsumer<ResourceLocation, Supplier<JsonElement>>) models::put);

        Method register = TTModelProvider.class.getDeclaredMethod("registerAdvancedAlchemicalFurnaceItem");
        register.setAccessible(true);
        register.invoke(provider);

        JsonObject model = models.get(
                        ResourceLocation.fromNamespaceAndPath("thaumaturge", "item/advanced_alchemical_furnace"))
                .get()
                .getAsJsonObject();
        assertEquals("minecraft:builtin/entity", model.get("parent").getAsString());
        JsonObject display = model.getAsJsonObject("display");
        assertNotNull(display);
        assertEquals(
                "[30.0,225.0,0.0]",
                display.getAsJsonObject("gui").get("rotation").toString());
        assertEquals(
                "[0.0,-2.75,0.0]",
                display.getAsJsonObject("gui").get("translation").toString());
        assertEquals(
                "[0.0,-3.5,0.0]",
                display.getAsJsonObject("fixed").get("translation").toString());
        assertEquals(
                "[0.625,0.625,0.625]",
                display.getAsJsonObject("fixed").get("scale").toString());
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = TTModelProvider.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
