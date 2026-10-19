package com.leclowndu93150.thaumaturge.content.taint;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSeedRegistry;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class TaintSeedInfluenceLoadedAnchorTest {
    @Test
    void unloadedSeedAnchorIsRetainedUntilItsEntitiesAreLoadedAndValidated() {
        ServerLevel level = mock(ServerLevel.class);
        TaintSeedRegistry registry = new TaintSeedRegistry();
        BlockPos seed = new BlockPos(32, 64, -16);
        registry.addSeed(seed);
        when(level.areEntitiesLoaded(ChunkPos.asLong(seed))).thenReturn(false, true);
        when(level.getEntitiesOfClass(eq(AbstractTaintSeed.class), any(AABB.class)))
                .thenReturn(List.of());

        try (MockedStatic<TaintSeedRegistry> lookup = mockStatic(TaintSeedRegistry.class)) {
            lookup.when(() -> TaintSeedRegistry.get(level)).thenReturn(registry);

            assertFalse(TaintHelper.isWithinSeedInfluence(level, seed));
            assertTrue(registry.all().contains(seed));
            verify(level, never()).getEntitiesOfClass(eq(AbstractTaintSeed.class), any(AABB.class));

            assertFalse(TaintHelper.isWithinSeedInfluence(level, seed));
            assertFalse(registry.all().contains(seed));
        }
    }
}
