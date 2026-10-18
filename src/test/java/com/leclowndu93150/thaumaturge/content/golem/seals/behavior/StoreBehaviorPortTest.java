package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class StoreBehaviorPortTest {
    @Test
    void onlyItemsActuallyAcceptedByTheDestinationCountAsDelivered() {
        ItemStack rejected = new ItemStack(Items.STONE, 3);
        assertEquals(0, StoreBehavior.placedCount(3, rejected));
        assertEquals(5, StoreBehavior.placedCount(8, rejected));
        assertEquals(8, StoreBehavior.placedCount(8, ItemStack.EMPTY));
    }

    @Test
    void looseItemsHaveNoVerticalKickAndOnlySmallTriangularDrift() {
        RandomSource random = mock(RandomSource.class);
        when(random.triangle(0.0, 0.02)).thenReturn(0.01, -0.02);

        assertEquals(new Vec3(0.01, 0.0, -0.02), StoreBehavior.tossVelocity(random));
        verify(random, times(2)).triangle(0.0, 0.02);
    }
}
