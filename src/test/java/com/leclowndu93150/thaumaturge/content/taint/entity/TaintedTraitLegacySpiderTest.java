package com.leclowndu93150.thaumaturge.content.taint.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitModifiers;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.junit.jupiter.api.Test;

class TaintedTraitLegacySpiderTest {
    @Test
    void legacySpiderlingStatsAreScopedToItsMigrationMarker() {
        MobTraitModifiers modifiers = mock(MobTraitModifiers.class);

        TaintedTrait.legacySpiderlingModifiers(modifiers);

        verify(modifiers).setBase(Attributes.MAX_HEALTH, 5.0);
        verify(modifiers).setBase(Attributes.ATTACK_DAMAGE, 2.0);
        verify(modifiers).setBase(Attributes.FOLLOW_RANGE, 12.0);
        verify(modifiers).setBase(Attributes.SCALE, 0.4);
    }
}
