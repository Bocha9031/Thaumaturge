package com.leclowndu93150.thaumaturge.content.spell.block;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class BlockEntityHoleSparkleTest {
    @Test
    void sparkleEdgesNeedAnOpenSideAndSolidBacking() {
        assertFalse(BlockEntityHole.isExposed(true, true, false, false, false));
        assertFalse(BlockEntityHole.isExposed(false, false, true, true, true));
        assertTrue(BlockEntityHole.isExposed(true, false, false, true, false));
        assertTrue(BlockEntityHole.isExposed(false, true, true, false, false));
        assertTrue(BlockEntityHole.isExposed(true, false, false, false, true));
    }
}
