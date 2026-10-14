package com.leclowndu93150.thaumaturge.content.aura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.doubleThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class AuraPollutionTest {
    private ServerLevel level;
    private RandomSource random;

    @BeforeEach
    void setUp() {
        AuraManager.resetSession();
        level = mock(ServerLevel.class);
        random = mock(RandomSource.class);
        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        when(level.getChunkSource()).thenReturn(chunkSource);
        when(level.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(0.0, 0.5, 0.75);
    }

    @AfterEach
    void tearDown() {
        AuraManager.resetSession();
    }

    @Test
    void pollutionUsesSourceRandomizedInsetAndSkipsHiddenOrNonPositiveEffects() {
        BlockPos pos = new BlockPos(10, 20, -5);
        try (MockedStatic<PacketDistributor> packets = mockStatic(PacketDistributor.class)) {
            AuraManager.taint(level, pos, 3.0F, false);
            AuraManager.taint(level, pos, 0.0F, true);
            AuraManager.taint(level, pos, -1.0F, true);
            packets.verifyNoInteractions();
            verify(random, never()).nextDouble();

            AuraManager.taint(level, pos, 2.0F, true);

            ArgumentCaptor<ClientboundSpawnParticlePayload> payloadCaptor =
                    ArgumentCaptor.forClass(ClientboundSpawnParticlePayload.class);
            packets.verify(() -> PacketDistributor.sendToPlayersNear(
                    eq(level),
                    isNull(),
                    doubleThat(value -> Math.abs(value - 10.2) < 1.0E-9),
                    doubleThat(value -> Math.abs(value - 20.5) < 1.0E-9),
                    doubleThat(value -> Math.abs(value - (-4.35)) < 1.0E-9),
                    eq(64.0),
                    payloadCaptor.capture()));

            ClientboundSpawnParticlePayload payload = payloadCaptor.getValue();
            assertEquals(TTParticles.POLLUTION_FUME.get(), payload.options());
            assertEquals(10.2, payload.x(), 1.0E-9);
            assertEquals(20.5, payload.y(), 1.0E-9);
            assertEquals(-4.35, payload.z(), 1.0E-9);
            assertEquals(0.0, payload.vx());
            assertEquals(0.0, payload.vy());
            assertEquals(0.0, payload.vz());
            verify(random, times(3)).nextDouble();
        }
    }
}
