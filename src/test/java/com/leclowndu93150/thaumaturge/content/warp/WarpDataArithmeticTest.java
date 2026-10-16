package com.leclowndu93150.thaumaturge.content.warp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import org.junit.jupiter.api.Test;

class WarpDataArithmeticTest {
    @Test
    void addUsesWideArithmeticBeforeClampingEveryPool() {
        for (WarpType type : WarpType.values()) {
            WarpData data = new WarpData();
            data.set(type, 1);

            assertEquals(WarpData.MAX_WARP, data.add(type, Integer.MAX_VALUE));
            assertEquals(0, data.add(type, Integer.MIN_VALUE));
        }
    }

    @Test
    void reduceUsesWideArithmeticWithoutNegatingTheAmount() {
        for (WarpType type : WarpType.values()) {
            WarpData data = new WarpData();
            data.set(type, 1);

            assertEquals(WarpData.MAX_WARP, data.reduce(type, Integer.MIN_VALUE));
            assertEquals(0, data.reduce(type, Integer.MAX_VALUE));
        }
    }

    @Test
    void addAndReducePreserveOrdinarySignedUpdatesAndClamps() {
        for (WarpType type : WarpType.values()) {
            WarpData data = new WarpData();

            assertEquals(12, data.add(type, 12));
            assertEquals(7, data.add(type, -5));
            assertEquals(10, data.reduce(type, -3));
            assertEquals(0, data.reduce(type, 20));
            assertEquals(WarpData.MAX_WARP, data.reduce(type, -Integer.MAX_VALUE));
        }
    }
}
