package com.leclowndu93150.thaumaturge.content.pech;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class PechBarterTest {
    @Test
    void payoutRoundsUseAtLeastHalfTheRemainingBudgetAndCapAtFive() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(5)).thenReturn(0, 2, 3, 4);

        assertEquals(3, PechBarter.roundSize(5, random));
        assertEquals(3, PechBarter.roundSize(5, random));
        assertEquals(4, PechBarter.roundSize(5, random));
        assertEquals(5, PechBarter.roundSize(5, random));

        when(random.nextInt(20)).thenReturn(0);
        assertEquals(5, PechBarter.roundSize(20, random));
    }

    @Test
    void haggleSwingUsesConfiguredGenerousNeutralAndGrumpyBands() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.1F, 0.3F, 0.7F);
        when(random.nextInt(3)).thenReturn(2, 1);

        assertEquals(2, PechBarter.swing(random));
        assertEquals(-1, PechBarter.swing(random));
        assertEquals(0, PechBarter.swing(random));
    }
}
