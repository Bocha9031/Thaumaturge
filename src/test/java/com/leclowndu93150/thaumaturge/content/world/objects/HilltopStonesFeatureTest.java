package com.leclowndu93150.thaumaturge.content.world.objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import org.junit.jupiter.api.Test;

class HilltopStonesFeatureTest {
    @Test
    void acceptsLevelGroundWithinThePlatformFootprint() {
        WorldGenLevel level = mock(WorldGenLevel.class);
        when(level.getHeight(eq(Heightmap.Types.WORLD_SURFACE), anyInt(), anyInt()))
                .thenReturn(91);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.GRASS_BLOCK.defaultBlockState());

        assertTrue(HilltopStonesFeature.siteIsLevel(level, new BlockPos(0, 90, 0)));
    }

    @Test
    void rejectsAPlatformFootprintWithAThreeBlockRise() {
        WorldGenLevel level = mock(WorldGenLevel.class);
        when(level.getHeight(eq(Heightmap.Types.WORLD_SURFACE), anyInt(), anyInt()))
                .thenAnswer(invocation ->
                        invocation.getArgument(1, Integer.class) == -2 && invocation.getArgument(2, Integer.class) == -2
                                ? 94
                                : 91);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.GRASS_BLOCK.defaultBlockState());

        assertFalse(HilltopStonesFeature.siteIsLevel(level, new BlockPos(0, 90, 0)));
    }

    @Test
    void groundLookupStepsThroughCommonSurfaceCover() {
        WorldGenLevel level = mock(WorldGenLevel.class);
        when(level.getHeight(eq(Heightmap.Types.WORLD_SURFACE), eq(4), eq(7))).thenReturn(93);
        when(level.getBlockState(any(BlockPos.class))).thenAnswer(invocation -> {
            BlockPos pos = invocation.getArgument(0);
            return switch (pos.getY()) {
                case 92 -> Blocks.SNOW.defaultBlockState();
                case 91 -> Blocks.TALL_GRASS.defaultBlockState();
                default -> Blocks.GRASS_BLOCK.defaultBlockState();
            };
        });

        assertEquals(new BlockPos(4, 90, 7), HilltopStonesFeature.groundAt(level, 4, 7));
    }
}
