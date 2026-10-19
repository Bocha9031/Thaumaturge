package com.leclowndu93150.thaumaturge.content.effect;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class BlockSparklesTest {
    @Test
    void unloadedNeighborIsSkippedWithoutReadingItsState() {
        ServerLevel level = mock(ServerLevel.class);
        BlockPos origin = new BlockPos(15, 64, 0);
        BlockPos neighbor = origin.east();
        when(level.isLoaded(neighbor)).thenReturn(false);

        assertFalse(Effects.BlockSparkles.isFaceVisible(level, origin, Direction.EAST));

        verify(level, never()).getBlockState(neighbor);
    }

    @Test
    void onlySolidSturdyNeighborFacesHideSparkles() {
        ServerLevel level = mock(ServerLevel.class);
        BlockPos origin = new BlockPos(0, 64, 0);
        BlockPos neighbor = origin.east();
        BlockState state = mock(BlockState.class);
        when(level.isLoaded(neighbor)).thenReturn(true);
        when(level.getBlockState(neighbor)).thenReturn(state);
        when(state.isSolidRender(level, neighbor)).thenReturn(true);
        when(state.isFaceSturdy(level, neighbor, Direction.WEST)).thenReturn(true);

        assertFalse(Effects.BlockSparkles.isFaceVisible(level, origin, Direction.EAST));

        when(state.isFaceSturdy(level, neighbor, Direction.WEST)).thenReturn(false);
        assertTrue(Effects.BlockSparkles.isFaceVisible(level, origin, Direction.EAST));
    }
}
