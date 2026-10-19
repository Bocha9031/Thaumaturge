package com.leclowndu93150.thaumaturge.content.entity.construct;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

final class ArcaneBoreOwnerFacingTest {
    private static final BlockPos BORE_POS = BlockPos.ZERO;

    @Test
    void nearbyOwnerCanAimTheBoreUpOrDown() {
        assertEquals(Direction.UP, EntityArcaneBore.placementFacing(BORE_POS, 0.5, 0.5, 2.6, Direction.NORTH));
        assertEquals(Direction.DOWN, EntityArcaneBore.placementFacing(BORE_POS, 0.5, 0.5, -0.1, Direction.NORTH));
    }

    @Test
    void otherwiseBoreFacesOppositeTheOwnersLookDirection() {
        assertEquals(Direction.SOUTH, EntityArcaneBore.placementFacing(BORE_POS, 3.0, 0.5, 4.0, Direction.NORTH));
        assertEquals(Direction.EAST, EntityArcaneBore.placementFacing(BORE_POS, 0.5, 0.5, 1.0, Direction.WEST));
    }
}
