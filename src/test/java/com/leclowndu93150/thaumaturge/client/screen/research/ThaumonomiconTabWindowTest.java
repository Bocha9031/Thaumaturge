package com.leclowndu93150.thaumaturge.client.screen.research;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class ThaumonomiconTabWindowTest {
    @Test
    void scrollingUsesZeroBasedVisibleRangeAndClampsToLastFullPage() {
        ThaumonomiconBrowserScreen.TabWindow window = ThaumonomiconBrowserScreen.tabWindow(9, 99, 100, 24);

        assertEquals(5, window.firstIndex());
        assertEquals(4, window.visibleCount());
        assertEquals(5, window.maxScroll());
        assertEquals(2, window.verticalShift());
    }

    @Test
    void shortAndEmptyListsResetScrollAndDoNotShiftTabs() {
        ThaumonomiconBrowserScreen.TabWindow shortList = ThaumonomiconBrowserScreen.tabWindow(2, 7, 100, 24);
        ThaumonomiconBrowserScreen.TabWindow emptyList = ThaumonomiconBrowserScreen.tabWindow(0, 4, 100, 24);

        assertEquals(0, shortList.firstIndex());
        assertEquals(0, shortList.maxScroll());
        assertEquals(0, shortList.verticalShift());
        assertEquals(0, emptyList.firstIndex());
        assertEquals(0, emptyList.maxScroll());
        assertEquals(0, emptyList.verticalShift());
    }
}
