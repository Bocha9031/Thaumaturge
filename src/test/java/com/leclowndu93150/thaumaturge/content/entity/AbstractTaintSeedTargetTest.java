package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AbstractTaintSeedTargetTest {
    @Test
    void onlyLivingNonSpectatorNonTaintedTargetsAreAfflicted() {
        LivingEntity seed = mock(LivingEntity.class);
        LivingEntity target = mock(LivingEntity.class);
        when(target.isAlive()).thenReturn(true);
        when(target.isSpectator()).thenReturn(false);

        try (MockedStatic<MobTraits> traits = mockStatic(MobTraits.class)) {
            traits.when(() -> MobTraits.isTainted(target)).thenReturn(false);
            assertTrue(AbstractTaintSeed.canAfflict(seed, target));
            when(target.isSpectator()).thenReturn(true);
            assertFalse(AbstractTaintSeed.canAfflict(seed, target));
            when(target.isSpectator()).thenReturn(false);
            when(target.isAlive()).thenReturn(false);
            assertFalse(AbstractTaintSeed.canAfflict(seed, target));
            assertFalse(AbstractTaintSeed.canAfflict(seed, seed));
        }
    }

    @Test
    void taintedTargetsRemainImmune() {
        LivingEntity seed = mock(LivingEntity.class);
        LivingEntity target = mock(LivingEntity.class);
        when(target.isAlive()).thenReturn(true);
        when(target.isSpectator()).thenReturn(false);

        try (MockedStatic<MobTraits> traits = mockStatic(MobTraits.class)) {
            traits.when(() -> MobTraits.isTainted(target)).thenReturn(true);
            assertFalse(AbstractTaintSeed.canAfflict(seed, target));
        }
    }
}
