package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

final class ThaumicSlimeSpawnPortTest {
    @Test
    void spawnRollSelectsMainBranchSizes() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(3)).thenReturn(0, 1, 2);

        assertEquals(1, ThaumicSlime.spawnSize(random));
        assertEquals(2, ThaumicSlime.spawnSize(random));
        assertEquals(4, ThaumicSlime.spawnSize(random));
    }

    @Test
    void aLoadedSlimeDoesNotRetainTransientLaunchedState() {
        ThaumicSlime slime = new ThaumicSlime(TTEntities.THAUMIC_SLIME.get(), mock(Level.class));
        assertTrue(slime.isLaunched());

        slime.readAdditionalSaveData(new CompoundTag());

        assertFalse(slime.isLaunched());
    }
}
