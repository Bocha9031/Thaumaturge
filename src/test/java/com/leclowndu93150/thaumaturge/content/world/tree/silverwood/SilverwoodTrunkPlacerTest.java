package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;

final class SilverwoodTrunkPlacerTest {
    @Test
    void crownHasAVolumetricCoreAndRandomizedOuterEdge() {
        assertEquals(0, SilverwoodTrunkPlacer.verticalOffset(-3));
        assertEquals(1, SilverwoodTrunkPlacer.verticalOffset(-4));
        assertEquals(2, SilverwoodTrunkPlacer.verticalOffset(-5));
        assertEquals(0, SilverwoodTrunkPlacer.verticalOffset(0));
        assertEquals(1, SilverwoodTrunkPlacer.verticalOffset(1));
        assertEquals(4, SilverwoodTrunkPlacer.verticalOffset(4));

        assertTrue(SilverwoodTrunkPlacer.fitsCrown(0, 0, 0, 10));
        assertTrue(SilverwoodTrunkPlacer.fitsCrown(3, 0, 0, 10));
        assertFalse(SilverwoodTrunkPlacer.fitsCrown(4, 0, 0, 10));
        assertTrue(SilverwoodTrunkPlacer.fitsCrown(4, 0, 0, 17));
    }

    @Test
    void spacingChecksNarrowTrunksAndBroadCanopiesInTheirOwnHeightBands() {
        assertTrue(SilverwoodTrunkPlacer.isCrowdingZone(2, 2, 0, 10));
        assertFalse(SilverwoodTrunkPlacer.isCrowdingZone(2, 2, 1, 10));
        assertTrue(SilverwoodTrunkPlacer.isCrowdingZone(5, 5, 5, 10));
        assertTrue(SilverwoodTrunkPlacer.isCrowdingZone(5, 15, 5, 10));
        assertFalse(SilverwoodTrunkPlacer.isCrowdingZone(5, 4, 5, 10));
        assertFalse(SilverwoodTrunkPlacer.isCrowdingZone(0, 16, 0, 10));
        assertFalse(SilverwoodTrunkPlacer.isCrowdingZone(0, -2, 0, 10));
    }

    @Test
    void crownFillsExistingLeavesButPreservesTheirDistance() {
        var oldLeaf = Blocks.OAK_LEAVES.defaultBlockState().setValue(BlockStateProperties.DISTANCE, 2);
        var newLeaf = Blocks.SPRUCE_LEAVES.defaultBlockState();

        var replacement = SilverwoodTrunkPlacer.leafReplacement(oldLeaf, newLeaf);

        assertEquals(2, replacement.getValue(BlockStateProperties.DISTANCE));
        assertNull(SilverwoodTrunkPlacer.leafReplacement(oldLeaf, oldLeaf));
        assertNull(SilverwoodTrunkPlacer.leafReplacement(Blocks.STONE.defaultBlockState(), newLeaf));
        assertEquals(newLeaf, SilverwoodTrunkPlacer.leafReplacement(Blocks.AIR.defaultBlockState(), newLeaf));
    }
}
