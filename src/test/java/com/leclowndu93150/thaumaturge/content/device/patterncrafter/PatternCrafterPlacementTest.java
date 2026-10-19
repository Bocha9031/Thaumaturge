package com.leclowndu93150.thaumaturge.content.device.patterncrafter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class PatternCrafterPlacementTest {
    @Test
    void frontFacesThePlacer() {
        BlockPatternCrafter block = TTBlocks.PATTERN_CRAFTER.get();
        BlockPlaceContext context = mock(BlockPlaceContext.class);
        Level level = mock(Level.class);
        when(context.getHorizontalDirection()).thenReturn(Direction.NORTH);
        when(context.getClickedPos()).thenReturn(BlockPos.ZERO);
        when(context.getLevel()).thenReturn(level);
        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(false);

        BlockState placed = block.getStateForPlacement(context);

        assertEquals(Direction.SOUTH, placed.getValue(BlockPatternCrafter.FACING));
    }
}
