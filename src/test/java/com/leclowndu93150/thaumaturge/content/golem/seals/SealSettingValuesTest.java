package com.leclowndu93150.thaumaturge.content.golem.seals;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class SealSettingValuesTest {
    private static final SealSetting SILK_TOUCH =
            new SealSetting("silk_touch", "gui.test.silk_touch", false).readingAlso("psilk");

    @Test
    void readsFormerKeyButWritesCanonicalKey() {
        var codec = SealSettingValues.codec(sealType()).codec();
        SealSettingValues decoded = codec.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"psilk\":true}"))
                .result()
                .orElseThrow();

        assertTrue(decoded.get(SILK_TOUCH));

        var encoded = codec.encodeStart(JsonOps.INSTANCE, decoded).result().orElseThrow().getAsJsonObject();
        assertTrue(encoded.get("silk_touch").getAsBoolean());
        assertFalse(encoded.has("psilk"));
    }

    @Test
    void canonicalValueTakesPrecedenceOverFormerKey() {
        var codec = SealSettingValues.codec(sealType()).codec();
        SealSettingValues decoded = codec.parse(
                        JsonOps.INSTANCE,
                        JsonParser.parseString("{\"silk_touch\":false,\"psilk\":true}"))
                .result()
                .orElseThrow();

        assertFalse(decoded.get(SILK_TOUCH));
    }

    private static SealType sealType() {
        return SealType.builder(() -> mock(ISealBehavior.class))
                .settings(SILK_TOUCH)
                .showSettings()
                .placer(() -> Items.STICK)
                .build();
    }
}
