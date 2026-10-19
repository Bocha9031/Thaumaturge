package com.leclowndu93150.thaumaturge.client.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class ThaumometerClientHandlerTest {
    @Test
    void targetLockUsesBlockPositionRatherThanObjectIdentity() {
        assertTrue(ThaumometerClientHandler.sameTarget(
                ScanTarget.block(new BlockPos(3, 64, -8)), ScanTarget.block(new BlockPos(3, 64, -8))));
        assertFalse(ThaumometerClientHandler.sameTarget(
                ScanTarget.block(new BlockPos(3, 64, -8)), ScanTarget.block(new BlockPos(4, 64, -8))));
    }

    @Test
    void skyTargetDoesNotMatchBlockTarget() {
        assertFalse(ThaumometerClientHandler.sameTarget(ScanTarget.sky(), ScanTarget.block(BlockPos.ZERO)));
        assertTrue(ThaumometerClientHandler.sameTarget(ScanTarget.sky(), ScanTarget.sky()));
    }
}
