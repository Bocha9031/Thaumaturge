package com.leclowndu93150.thaumaturge.client.color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.content.decor.HeldCandle;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.junit.jupiter.api.Test;

final class CandleHolderPortTest {
    private static final String RESOURCE_ROOT = "assets/thaumaturge/models/block/";

    @Test
    void registeredHolderKeepsParticleWhiteAndDyesOnlyHeldCandleTint() {
        BlockColors colors = new BlockColors();
        CandleBlockColors.onRegisterBlockColors(new RegisterColorHandlersEvent.Block(colors));

        for (DeferredBlock<BlockCandleHolder> holder : TTBlocks.CANDLE_HOLDERS.values()) {
            BlockState emptyState =
                    holder.get().defaultBlockState().setValue(BlockCandleHolder.CANDLE, HeldCandle.NONE);
            BlockState blueState = holder.get().defaultBlockState().setValue(BlockCandleHolder.CANDLE, HeldCandle.BLUE);

            assertEquals(0xFFFFFFFF, colors.getColor(emptyState, null, null, 0));
            assertEquals(0xFFFFFFFF, colors.getColor(blueState, null, null, 0));
            assertEquals(0xFFFFFFFF, colors.getColor(emptyState, null, null, 1));
            assertEquals(0xFF000000 | DyeColor.BLUE.getMapColor().col, colors.getColor(blueState, null, null, 1));
        }
    }

    @Test
    void candleModelsUseRegisteredTintSlotsAndLegalFlameRotations() throws IOException {
        JsonObject candle = readModel("candle.json");
        assertEquals(List.of(0), candleTintIndices(candle));
        assertFlameRotation(candle);

        for (String material : List.of("brass", "thaumium", "void")) {
            JsonObject filledHolder = readModel("candle_holder_" + material + "_filled.json");
            assertEquals(List.of(1), candleTintIndices(filledHolder));
            assertFlameRotation(filledHolder);
        }
    }

    private static JsonObject readModel(String name) throws IOException {
        String resource = RESOURCE_ROOT + name;
        try (InputStream stream = CandleHolderPortTest.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Missing model resource " + resource);
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }

    private static List<Integer> candleTintIndices(JsonObject model) {
        List<Integer> tintIndices = new ArrayList<>();
        for (JsonElement element : model.getAsJsonArray("elements")) {
            JsonObject faces = element.getAsJsonObject().getAsJsonObject("faces");
            for (Map.Entry<String, JsonElement> faceEntry : faces.entrySet()) {
                JsonObject face = faceEntry.getValue().getAsJsonObject();
                if ("#all".equals(face.get("texture").getAsString())) {
                    tintIndices.add(face.get("tintindex").getAsInt());
                }
            }
        }
        assertTrue(!tintIndices.isEmpty(), "model should contain candle-textured faces");
        return tintIndices.stream().distinct().sorted().toList();
    }

    private static void assertFlameRotation(JsonObject model) {
        boolean foundLegalFlameRotation = false;
        for (JsonElement element : model.getAsJsonArray("elements")) {
            JsonObject elementObject = element.getAsJsonObject();
            if (!elementObject.has("rotation")) {
                continue;
            }
            JsonObject rotation = elementObject.getAsJsonObject("rotation");
            if ("z".equals(rotation.get("axis").getAsString())) {
                assertEquals(22.5, rotation.get("angle").getAsDouble());
                foundLegalFlameRotation = true;
            }
        }
        assertTrue(foundLegalFlameRotation, "flame should retain the closest legal native model rotation");
    }
}
