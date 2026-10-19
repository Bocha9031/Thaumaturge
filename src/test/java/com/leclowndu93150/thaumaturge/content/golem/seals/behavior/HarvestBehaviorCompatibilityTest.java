package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class HarvestBehaviorCompatibilityTest {
    @Test
    void legacyReplantDataLoadsAndIsWrittenInTheCanonicalShape() {
        long position = new BlockPos(12, 64, -4).asLong();
        var legacy = JsonParser.parseString("""
                {"replant":[{"taskloc":%d,"taskface":1,"farmland":true}]}
                """.formatted(position));

        var behavior =
                HarvestBehavior.CODEC.codec().parse(JsonOps.INSTANCE, legacy).getOrThrow();
        var encoded = HarvestBehavior.CODEC
                .codec()
                .encodeStart(JsonOps.INSTANCE, behavior)
                .getOrThrow()
                .getAsJsonObject();
        var site = encoded.getAsJsonArray("replant_sites").get(0).getAsJsonObject();

        assertFalse(encoded.has("replant"));
        assertEquals(position, site.get("pos").getAsLong());
        assertEquals(1, site.get("face").getAsInt());
        assertTrue(site.get("tilled").getAsBoolean());
    }

    @Test
    void canonicalReplantDataTakesPrecedenceWhenBothFormsExist() {
        long currentPosition = new BlockPos(1, 2, 3).asLong();
        long legacyPosition = new BlockPos(4, 5, 6).asLong();
        var saved = JsonParser.parseString("""
                {"replant_sites":[{"pos":%d}],"replant":[{"taskloc":%d}]}
                """.formatted(currentPosition, legacyPosition));

        var behavior =
                HarvestBehavior.CODEC.codec().parse(JsonOps.INSTANCE, saved).getOrThrow();
        var encoded = HarvestBehavior.CODEC
                .codec()
                .encodeStart(JsonOps.INSTANCE, behavior)
                .getOrThrow()
                .getAsJsonObject();

        assertEquals(
                currentPosition,
                encoded.getAsJsonArray("replant_sites")
                        .get(0)
                        .getAsJsonObject()
                        .get("pos")
                        .getAsLong());
    }

    @Test
    void legacyBlockPositionEncodingAlsoLoads() {
        var legacy = JsonParser.parseString("""
                {"replant":[{"taskloc":[12,64,-4]}]}
                """);

        var behavior =
                HarvestBehavior.CODEC.codec().parse(JsonOps.INSTANCE, legacy).getOrThrow();
        var encoded = HarvestBehavior.CODEC
                .codec()
                .encodeStart(JsonOps.INSTANCE, behavior)
                .getOrThrow()
                .getAsJsonObject();

        assertEquals(
                new BlockPos(12, 64, -4).asLong(),
                encoded.getAsJsonArray("replant_sites")
                        .get(0)
                        .getAsJsonObject()
                        .get("pos")
                        .getAsLong());
    }
}
