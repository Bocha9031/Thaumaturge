package com.leclowndu93150.thaumaturge.content.entity.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

final class EntityEldritchKnockbackSyncTest {
    @Test
    void successfulGolemMeleeHitMarksItsVelocityChangeForSync() {
        ServerLevel level = mockServerLevel();
        EntityEldritchGolem golem = new EntityEldritchGolem(TTEntities.ELDRITCH_GOLEM.get(), level);
        DamageSources damageSources = mock(DamageSources.class);
        DamageSource damageSource = mock(DamageSource.class);
        Entity target = mock(Entity.class);
        when(level.damageSources()).thenReturn(damageSources);
        when(damageSources.mobAttack(golem)).thenReturn(damageSource);
        when(target.getDeltaMovement()).thenReturn(Vec3.ZERO);
        when(target.hurt(any(DamageSource.class), anyFloat())).thenReturn(true);

        assertTrue(golem.doHurtTarget(target));

        verify(target).setDeltaMovement(new Vec3(0.0D, 0.2D, 0.0D));
        assertTrue(target.hurtMarked);
    }

    @Test
    void missedGolemMeleeHitDoesNotMarkKnockback() {
        ServerLevel level = mockServerLevel();
        EntityEldritchGolem golem = new EntityEldritchGolem(TTEntities.ELDRITCH_GOLEM.get(), level);
        DamageSources damageSources = mock(DamageSources.class);
        DamageSource damageSource = mock(DamageSource.class);
        Entity target = mock(Entity.class);
        when(level.damageSources()).thenReturn(damageSources);
        when(damageSources.mobAttack(golem)).thenReturn(damageSource);
        when(target.getDeltaMovement()).thenReturn(Vec3.ZERO);
        when(target.hurt(any(DamageSource.class), anyFloat())).thenReturn(false);

        assertFalse(golem.doHurtTarget(target));

        assertFalse(target.hurtMarked);
    }

    @Test
    void wardenScreechMarksItsPushForSync() {
        EntityEldritchWarden warden = new EntityEldritchWarden(TTEntities.ELDRITCH_WARDEN.get(), mockServerLevel());
        LivingEntity target = mock(LivingEntity.class);
        warden.setYRot(0.0F);

        warden.screech(target);

        ArgumentCaptor<Double> horizontalPush = ArgumentCaptor.forClass(Double.class);
        verify(target).push(horizontalPush.capture(), eq(0.1D), eq(1.5D));
        assertEquals(0.0D, horizontalPush.getValue(), 1.0E-9D);
        assertTrue(target.hurtMarked);
    }

    private static ServerLevel mockServerLevel() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.getScoreboard()).thenReturn(mock(ServerScoreboard.class));
        return level;
    }
}
