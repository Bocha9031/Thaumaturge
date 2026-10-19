package com.leclowndu93150.thaumaturge.content.entity.construct;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

final class StationaryNavigationTest {
    @Test
    void constructNavigationDoesNotRecomputeMovingPaths() {
        StationaryNavigation navigation = new StationaryNavigation(mock(Mob.class), mock(Level.class));

        assertFalse(navigation.canUpdatePath());
    }
}
