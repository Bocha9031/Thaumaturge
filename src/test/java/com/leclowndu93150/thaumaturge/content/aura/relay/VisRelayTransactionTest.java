package com.leclowndu93150.thaumaturge.content.aura.relay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.transaction.SnapshotJournal;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.junit.jupiter.api.Test;

class VisRelayTransactionTest {
    @Test
    void simulatedDrainUsesAvailabilityWithoutMutatingOrTriggeringTheRelay() {
        ResourceSetup resources = resources();
        VisRelayNetwork network = new VisRelayNetwork();

        assertEquals(30, network.drainCentivis(resources.level(), BlockPos.ZERO, TTAspects.AER, 30, true));
        assertEquals(100, resources.source().stock());
        assertEquals(0, resources.source().drainCalls());
        verify(resources.relay(), never()).triggerConsumeEffect(resources.level(), resources.aspect());

        assertEquals(100, network.drainCentivis(resources.level(), BlockPos.ZERO, TTAspects.AER, 120, true));
        assertEquals(100, resources.source().stock());
        assertEquals(0, resources.source().drainCalls());
    }

    @Test
    void drainRollbackRestoresSourceAndDefersRelayPulseUntilRootCommit() {
        ResourceSetup resources = resources();
        VisRelayNetwork network = new VisRelayNetwork();

        try (Transaction root = Transaction.openRoot()) {
            assertEquals(30, network.drainCentivis(resources.level(), BlockPos.ZERO, TTAspects.AER, 30, root));
            assertEquals(70, resources.source().stock());
            verify(resources.relay(), never()).triggerConsumeEffect(resources.level(), resources.aspect());
        }

        assertEquals(100, resources.source().stock());
        verify(resources.relay(), never()).triggerConsumeEffect(resources.level(), resources.aspect());

        try (Transaction root = Transaction.openRoot()) {
            try (Transaction child = Transaction.open(root)) {
                assertEquals(30, network.drainCentivis(resources.level(), BlockPos.ZERO, TTAspects.AER, 30, child));
                child.commit();
            }
            assertEquals(70, resources.source().stock());
            verify(resources.relay(), never()).triggerConsumeEffect(resources.level(), resources.aspect());
            root.commit();
        }

        assertEquals(70, resources.source().stock());
        verify(resources.relay()).triggerConsumeEffect(resources.level(), resources.aspect());
    }

    private static ResourceSetup resources() {
        BlockPos relayPos = new BlockPos(1, 1, 1);
        ServerLevel level = mock(ServerLevel.class);
        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        LevelChunk chunk = mock(LevelChunk.class);
        when(level.getChunkSource()).thenReturn(chunkSource);
        BlockEntityVisRelay relay = mock(BlockEntityVisRelay.class);
        when(chunk.getBlockEntities()).thenReturn(Map.of(relayPos, relay));
        when(chunkSource.getChunkNow(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .thenAnswer(call -> call.getArgument(0, Integer.class) == 0 && call.getArgument(1, Integer.class) == 0
                        ? chunk
                        : null);
        when(relay.getBlockPos()).thenReturn(relayPos);
        when(relay.isLinked()).thenReturn(true);

        MappedRegistry<IAspect> aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        IAspect aspectValue = mock(IAspect.class);
        Registry.register(aspects, TTAspects.AER, aspectValue);
        RegistryAccess registryAccess = new RegistryAccess.ImmutableRegistryAccess(List.of(aspects)).freeze();
        when(level.registryAccess()).thenReturn(registryAccess);
        Holder<IAspect> aspect = aspects.getHolderOrThrow(TTAspects.AER);

        JournaledSource source = new JournaledSource(100);
        when(relay.resolveSource(level)).thenReturn(new LinkedRelaySource(relayPos, source));
        return new ResourceSetup(level, relay, source, aspect);
    }

    private record ResourceSetup(
            ServerLevel level, BlockEntityVisRelay relay, JournaledSource source, Holder<IAspect> aspect) {}

    private static final class JournaledSource implements IVisRelaySource {
        private final StockJournal journal = new StockJournal();
        private int stock;
        private int drainCalls;

        JournaledSource(int stock) {
            this.stock = stock;
        }

        int stock() {
            return stock;
        }

        int drainCalls() {
            return drainCalls;
        }

        @Override
        public boolean canSupply() {
            return stock > 0;
        }

        @Override
        public int availableCentivis(ResourceKey<IAspect> primal) {
            return stock;
        }

        @Override
        public int drainCentivis(ResourceKey<IAspect> primal, int amount, TransactionContext transaction) {
            drainCalls++;
            int taken = Math.min(stock, amount);
            if (taken > 0) {
                journal.updateSnapshots(transaction);
                stock -= taken;
            }
            return taken;
        }

        private final class StockJournal extends SnapshotJournal<Integer> {
            @Override
            protected Integer createSnapshot() {
                return stock;
            }

            @Override
            protected void revertToSnapshot(Integer snapshot) {
                stock = snapshot;
            }
        }
    }
}
