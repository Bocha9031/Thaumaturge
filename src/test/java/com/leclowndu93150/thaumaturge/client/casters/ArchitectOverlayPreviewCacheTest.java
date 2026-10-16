package com.leclowndu93150.thaumaturge.client.casters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

class ArchitectOverlayPreviewCacheTest {
    @Test
    void refreshesOnDistinctAnchorsWithCollidingConcatenatedCoordinates() {
        ArchitectOverlayRenderer.PreviewCache cache = new ArchitectOverlayRenderer.PreviewCache();
        BlockPos firstAnchor = new BlockPos(1, 23, 4);
        BlockPos secondAnchor = new BlockPos(12, 3, 4);

        assertTrue(cache.update(firstAnchor, Direction.UP, 7));
        assertTrue(cache.update(secondAnchor, Direction.UP, 7));
        assertFalse(cache.update(secondAnchor, Direction.UP, 7));
    }

    @Test
    void refreshesWhenFaceOrTimeBucketChanges() {
        ArchitectOverlayRenderer.PreviewCache cache = new ArchitectOverlayRenderer.PreviewCache();
        BlockPos anchor = new BlockPos(3, 5, 8);

        assertTrue(cache.update(anchor, Direction.UP, 7));
        assertTrue(cache.update(anchor, Direction.NORTH, 7));
        assertTrue(cache.update(anchor, Direction.NORTH, 8));
        assertFalse(cache.update(anchor, Direction.NORTH, 8));
    }
}
