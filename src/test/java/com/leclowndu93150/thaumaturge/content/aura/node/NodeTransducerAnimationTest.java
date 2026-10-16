package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class NodeTransducerAnimationTest {
    @Test
    void firstSnapshotIsImmediateButLaterSnapshotsInterpolateAtSubTickResolution() {
        Level level = mock(Level.class);
        when(level.isClientSide()).thenReturn(true);
        BlockEntityNodeTransducer transducer = transducer(level);
        update(transducer, 10);
        assertEquals(10.0F, transducer.getRenderCount(0.5F));

        when(level.getGameTime()).thenReturn(10L);
        update(transducer, 20);
        assertEquals(20, transducer.getCount());
        assertEquals(10.0F, transducer.getRenderCount(0.0F));
        assertEquals(10.5F, transducer.getRenderCount(0.5F));
        when(level.getGameTime()).thenReturn(15L);
        assertEquals(15.5F, transducer.getRenderCount(0.5F));
        when(level.getGameTime()).thenReturn(20L);
        assertEquals(20.0F, transducer.getRenderCount(0.0F));
        when(level.getGameTime()).thenReturn(100L);
        assertEquals(20.0F, transducer.getRenderCount(0.5F));
    }

    @Test
    void interruptedAnimationContinuesFromItsVisiblePositionAndRetractsSmoothly() {
        Level level = mock(Level.class);
        when(level.isClientSide()).thenReturn(true);
        BlockEntityNodeTransducer transducer = transducer(level);
        update(transducer, 0);
        update(transducer, 10);
        when(level.getGameTime()).thenReturn(5L);
        assertEquals(5.0F, transducer.getRenderCount(0.0F));
        update(transducer, 0);
        assertEquals(5.0F, transducer.getRenderCount(0.0F));
        when(level.getGameTime()).thenReturn(10L);
        assertEquals(2.5F, transducer.getRenderCount(0.0F));
        when(level.getGameTime()).thenReturn(15L);
        assertEquals(0.0F, transducer.getRenderCount(0.0F));
        assertEquals(0, transducer.getCount());
    }

    @Test
    void serverAndUnattachedEntitiesKeepTheirExactCountAndSavedData() {
        BlockEntityNodeTransducer unattached = transducer(null);
        update(unattached, -1);
        assertEquals(0.0F, unattached.getRenderCount(0.5F));
        Level server = mock(Level.class);
        BlockEntityNodeTransducer transducer = transducer(server);
        update(transducer, 40);
        update(transducer, 30);
        assertEquals(30.0F, transducer.getRenderCount(0.5F));
        CompoundTag saved = new CompoundTag();
        transducer.saveAdditional(saved, mock(HolderLookup.Provider.class));
        assertEquals(30, saved.getInt("Count"));
        assertEquals(1, saved.getInt("Status"));
        assertEquals(2, saved.getAllKeys().size());
    }

    private static BlockEntityNodeTransducer transducer(Level level) {
        BlockEntityNodeTransducer transducer = new BlockEntityNodeTransducer(
                BlockPos.ZERO, TTBlocks.NODE_TRANSDUCER.get().defaultBlockState());
        if (level != null) transducer.setLevel(level);
        return transducer;
    }

    private static void update(BlockEntityNodeTransducer transducer, int count) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Count", count);
        tag.putInt("Status", 1);
        transducer.loadAdditional(tag, mock(HolderLookup.Provider.class));
    }
}
