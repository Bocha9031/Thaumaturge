package com.leclowndu93150.thaumaturge.content.workbench;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class WorkbenchAuraPaymentTest {
    @Test
    void chargerBalancesPaymentAcrossFragmentedHalfVisChunks() {
        List<BlockPos> anchors = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            anchors.add(new BlockPos(i, 0, 0));
        }
        float[] stock = new float[anchors.size()];
        Arrays.fill(stock, 0.5F);

        assertTrue(BlockEntityArcaneWorkbench.drainAcrossAnchors(4.5D, anchors, (anchor, request) -> {
            int index = anchor.getX();
            float supplied = Math.min(stock[index], request);
            stock[index] -= supplied;
            return supplied;
        }));
    }

    @Test
    void chargerStopsWhenNoLoadedAnchorCanCoverTheRemainder() {
        List<BlockPos> anchors = List.of(BlockPos.ZERO);
        float[] stock = {0.5F};

        assertFalse(BlockEntityArcaneWorkbench.drainAcrossAnchors(2.0D, anchors, (anchor, request) -> {
            float supplied = Math.min(stock[0], request);
            stock[0] -= supplied;
            return supplied;
        }));
    }
}
