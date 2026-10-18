package com.leclowndu93150.thaumaturge.content.golem.logistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MenuGolemLogisticsPortTest {
    @Test
    void performsTheFirstStockScanEvenAtWorldTimeZero() {
        assertTrue(MenuGolemLogistics.shouldCollectStock(false, 0, Long.MIN_VALUE));
    }

    @Test
    void throttlesSubsequentStockScansUntilTheRefreshInterval() {
        assertFalse(MenuGolemLogistics.shouldCollectStock(true, 19, 0));
        assertTrue(MenuGolemLogistics.shouldCollectStock(true, 20, 0));
    }

    @Test
    void lastPageUsesVisibleRowsWithoutAnExtraBlankPage() {
        assertEquals(0, MenuGolemLogistics.lastPageForItemCount(0));
        assertEquals(0, MenuGolemLogistics.lastPageForItemCount(80));
        assertEquals(0, MenuGolemLogistics.lastPageForItemCount(81));
        assertEquals(1, MenuGolemLogistics.lastPageForItemCount(82));
    }

    @Test
    void refreshClampsPageWhenTheResultSetShrinks() {
        assertEquals(1, MenuGolemLogistics.clampPage(3, 82));
        assertEquals(0, MenuGolemLogistics.clampPage(3, 81));
        assertEquals(0, MenuGolemLogistics.clampPage(-1, 82));
    }
}
