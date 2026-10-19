package com.leclowndu93150.thaumaturge.data.loot;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class TTBlockLootSubProviderTest {
    @Test
    void taintRockFortuneChanceTableScalesThroughAllFifteenEntries() throws Exception {
        Field chancesField = TTBlockLootSubProvider.class.getDeclaredField("TAINT_ROCK_CRYSTAL_CHANCES");
        chancesField.setAccessible(true);

        float[] expected = new float[15];
        for (int index = 0; index < expected.length; index++) {
            expected[index] = (index + 1) / 15.0F;
        }

        assertArrayEquals(expected, (float[]) chancesField.get(null), 0.00001F);
    }
}
