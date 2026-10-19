package com.leclowndu93150.thaumaturge.content.misc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import org.junit.jupiter.api.Test;

class ItemCurioPortTest {
    @Test
    void ritesThresholdIncludesTemporaryWarpAndRequiresMoreThanTwenty() {
        IPlayerWarp warp = mock(IPlayerWarp.class);
        when(warp.get(WarpType.NORMAL)).thenReturn(8);
        when(warp.get(WarpType.PERMANENT)).thenReturn(7);
        when(warp.get(WarpType.TEMPORARY)).thenReturn(5);

        assertEquals(20, ItemCurio.totalWarp(warp));
        assertFalse(ItemCurio.canReadRites(ItemCurio.totalWarp(warp)));
        when(warp.get(WarpType.TEMPORARY)).thenReturn(6);
        assertTrue(ItemCurio.canReadRites(ItemCurio.totalWarp(warp)));
    }
}
