package com.leclowndu93150.thaumaturge.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class NodeDeviceItemModelTest {
    @Test
    void customRenderedNodeDevicesUseTheBuiltinEntityParent() throws IOException {
        try (var input = NodeDeviceItemModelTest.class
                .getClassLoader()
                .getResourceAsStream("assets/thaumaturge/models/item/node_stabilizer_base.json")) {
            assertNotNull(input);
            JsonObject model = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            assertEquals("minecraft:builtin/entity", model.get("parent").getAsString());
        }
    }

    @Test
    void sharedCustomBlockItemModelUsesTheBuiltinEntityParent() throws IOException {
        try (var input = NodeDeviceItemModelTest.class
                .getClassLoader()
                .getResourceAsStream("assets/thaumaturge/models/item/special_block_item_base.json")) {
            assertNotNull(input);
            JsonObject model = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            assertEquals("minecraft:builtin/entity", model.get("parent").getAsString());
        }
    }
}
