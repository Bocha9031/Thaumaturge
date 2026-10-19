package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.Test;

class FortressArmorEventsTest {
    @Test
    void prefersOwningEntityOverDirectEntity() {
        DamageSource source = mock(DamageSource.class);
        LivingEntity owner = mock(LivingEntity.class);
        Entity direct = mock(Entity.class);
        when(source.getEntity()).thenReturn(owner);
        when(source.getDirectEntity()).thenReturn(direct);

        assertSame(owner, FortressArmorEvents.attackerOf(source));
        verify(source, never()).getDirectEntity();
    }

    @Test
    void usesDirectLivingEntityWhenNoOwnerExists() {
        DamageSource source = mock(DamageSource.class);
        LivingEntity direct = mock(LivingEntity.class);
        when(source.getDirectEntity()).thenReturn(direct);

        assertSame(direct, FortressArmorEvents.attackerOf(source));
    }

    @Test
    void returnsNullWhenDamageHasNoAttacker() {
        assertNull(FortressArmorEvents.attackerOf(mock(DamageSource.class)));
    }
}
