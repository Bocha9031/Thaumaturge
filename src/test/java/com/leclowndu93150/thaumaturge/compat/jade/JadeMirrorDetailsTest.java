package com.leclowndu93150.thaumaturge.compat.jade;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirror;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JadeMirrorDetailsTest {
    @Test
    void linkedMirrorDetailsCanBeSentThroughJade() {
        BlockEntityMirror mirror = Mockito.mock(BlockEntityMirror.class);
        mirror.linked = true;
        mirror.link = GlobalPos.of(
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld")),
                new BlockPos(1, 2, 3));
        JadeDetailBuilder builder = new JadeDetailBuilder(JadeConfig.MIRRORS, Mockito.mock(Player.class));

        JadeMachineDetails.mirror(mirror, builder);

        assertFalse(JadeBlockDetails.CODEC
                .encodeStart(JsonOps.INSTANCE, builder.build())
                .error()
                .isPresent());
    }
}
