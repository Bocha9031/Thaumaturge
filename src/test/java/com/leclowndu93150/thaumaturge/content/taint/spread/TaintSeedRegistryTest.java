package com.leclowndu93150.thaumaturge.content.taint.spread;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class TaintSeedRegistryTest {
    @Test
    void movingSeedReplacesItsOldInfluenceAnchor() {
        TaintSeedRegistry registry = new TaintSeedRegistry();
        BlockPos original = new BlockPos(2, 64, 3);
        BlockPos moved = new BlockPos(4, 65, 3);

        registry.moveSeed(null, original);
        registry.moveSeed(original, moved);

        assertEquals(List.of(moved), registry.all());
        assertTrue(registry.all().contains(moved));
    }

    @Test
    void repeatedPositionUpdateDoesNotDuplicateAnchor() {
        TaintSeedRegistry registry = new TaintSeedRegistry();
        BlockPos anchor = new BlockPos(2, 64, 3);

        registry.moveSeed(null, anchor);
        registry.moveSeed(anchor, anchor);

        assertEquals(List.of(anchor), registry.all());
    }
}
