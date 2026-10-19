package com.leclowndu93150.thaumaturge.api.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.resources.ResourceKey;
import org.junit.jupiter.api.Test;

class SpellNodeCodecTest {
    private static final ResourceKey<SpellPart> PART =
            ResourceKey.create(SpellPart.REGISTRY_KEY, TTIds.rl("codec_test"));
    private static final ResourceKey<IAspect> ASPECT = ResourceKey.create(IAspect.REGISTRY_KEY, TTIds.rl("codec_test"));

    @Test
    void diskAndNetworkCodecsRoundTripSpellNodeData() {
        SpellNode node = new SpellNode(PART, Optional.of(ASPECT), Map.of("strength", 3), List.of(SpellNode.of(PART)));

        var json = SpellNode.CODEC.encodeStart(JsonOps.INSTANCE, node).getOrThrow();
        assertEquals(node, SpellNode.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());

        ByteBuf buffer = Unpooled.buffer();
        try {
            SpellNode.STREAM_CODEC.encode(buffer, node);
            assertEquals(node, SpellNode.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void diskCodecRejectsTreesBeyondItsDepthAndWidthLimits() {
        assertTrue(SpellNode.CODEC
                .encodeStart(JsonOps.INSTANCE, chain(SpellNode.MAX_DEPTH + 1))
                .error()
                .isPresent());

        SpellNode tooWide = new SpellNode(
                PART,
                Optional.empty(),
                Map.of(),
                IntStream.range(0, SpellNode.MAX_CHILDREN + 1)
                        .mapToObj(ignored -> SpellNode.of(PART))
                        .toList());
        assertTrue(
                SpellNode.CODEC.encodeStart(JsonOps.INSTANCE, tooWide).error().isPresent());
    }

    @Test
    void bothCodecsRejectTooManySettingsAndNetworkEncodeRejectsExcessiveDepth() {
        Map<String, Integer> tooManySettings = IntStream.range(0, SpellNode.MAX_SETTINGS + 1)
                .boxed()
                .collect(java.util.stream.Collectors.toMap(index -> "setting_" + index, index -> index));
        SpellNode tooMany = new SpellNode(PART, Optional.empty(), tooManySettings, List.of());

        assertTrue(
                SpellNode.CODEC.encodeStart(JsonOps.INSTANCE, tooMany).error().isPresent());

        ByteBuf settingsBuffer = Unpooled.buffer();
        try {
            assertThrows(EncoderException.class, () -> SpellNode.STREAM_CODEC.encode(settingsBuffer, tooMany));
        } finally {
            settingsBuffer.release();
        }

        ByteBuf deepBuffer = Unpooled.buffer();
        try {
            assertThrows(
                    EncoderException.class,
                    () -> SpellNode.STREAM_CODEC.encode(deepBuffer, chain(SpellNode.MAX_DEPTH + 1)));
        } finally {
            deepBuffer.release();
        }
    }

    private static SpellNode chain(int depth) {
        SpellNode node = SpellNode.of(PART);
        for (int currentDepth = 1; currentDepth < depth; currentDepth++) {
            node = new SpellNode(PART, Optional.empty(), Map.of(), List.of(node));
        }
        return node;
    }
}
