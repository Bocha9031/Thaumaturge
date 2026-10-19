package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class EntityGolemOrbPortTest {
    @Test
    void entityHitDispatchesThroughProjectileAndAppliesTheWhiteOrbDamageFactor() {
        ServerLevel level = mock(ServerLevel.class);
        TestOrb orb = new TestOrb(orbType(), level);
        LivingEntity caster = mock(LivingEntity.class);
        AttributeMap attributes = mock(AttributeMap.class);
        Entity target = mock(Entity.class);
        DamageSources damageSources = mock(DamageSources.class);
        DamageSource damageSource = mock(DamageSource.class);
        when(caster.getAttributes()).thenReturn(attributes);
        when(attributes.hasAttribute(Attributes.ATTACK_DAMAGE)).thenReturn(true);
        when(caster.getAttributeValue(Attributes.ATTACK_DAMAGE)).thenReturn(10.0D);
        doReturn(EntityType.ZOMBIE).when(target).getType();
        when(level.damageSources()).thenReturn(damageSources);
        when(damageSources.indirectMagic(any(Entity.class), any(Entity.class))).thenReturn(damageSource);
        orb.setOwner(caster);

        orb.hitEntity(target);

        assertEquals(1, orb.entityHitCallbacks);
        verify(target).hurt(eq(damageSource), eq(6.0F));
        verify(level).gameEvent(eq(GameEvent.PROJECTILE_LAND), eq(Vec3.ZERO), any(GameEvent.Context.class));
        verify(level)
                .sendParticles(
                        eq(new SparkParticleOptions(0xD8E4FF, 0.9F, 2.0F)),
                        eq(0.0D),
                        eq(0.0D),
                        eq(0.0D),
                        eq(8),
                        eq(0.15D),
                        eq(0.15D),
                        eq(0.15D),
                        eq(0.02D));
    }

    @Test
    void serverExpiryRunsBeforeTheProjectileBaseTickAndOrbIsNotSaved() {
        ServerLevel level = mock(ServerLevel.class);
        TestOrb orb = new TestOrb(orbType(), level);
        orb.tickCount = 161;

        orb.tick();

        assertEquals(161, orb.tickCount);
        assertTrue(orb.isRemoved());
        assertFalse(orb.shouldBeSaved());
        assertTrue(orb.isPickable());
        assertEquals(0.5F, orb.getPickRadius());
        assertFalse(orb.accepts(orb));
    }

    @Test
    void invulnerableDeflectionIsRejectedAndOrdinaryDeflectionMarksTheOrbHurt() {
        ServerLevel level = mock(ServerLevel.class);
        TestOrb orb = new TestOrb(orbType(), level);
        Entity attacker = mock(Entity.class);
        DamageSource source = mock(DamageSource.class);
        when(source.getEntity()).thenReturn(attacker);
        when(attacker.getLookAngle()).thenReturn(new Vec3(1.0D, 0.0D, 0.0D));
        orb.setInvulnerable(true);

        assertFalse(orb.hurtServer(level, source, 1.0F));
        assertEquals(Vec3.ZERO, orb.getDeltaMovement());
        assertFalse(orb.wasMarkedHurt());

        orb.setInvulnerable(false);
        assertTrue(orb.hurtServer(level, source, 1.0F));
        assertEquals(new Vec3(0.9D, 0.0D, 0.0D), orb.getDeltaMovement());
        assertTrue(orb.wasMarkedHurt());
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends EntityGolemOrb> orbType() {
        EntityType<?> type = mock(EntityType.class);
        when(type.getDimensions()).thenReturn(EntityDimensions.scalable(0.25F, 0.25F));
        // The constructor uses this mocked type only for dimensions; no registry entry is needed.
        return (EntityType<? extends EntityGolemOrb>) type;
    }

    private static final class TestOrb extends EntityGolemOrb {
        private int entityHitCallbacks;

        private TestOrb(EntityType<? extends EntityGolemOrb> type, Level level) {
            super(type, level);
        }

        @Override
        protected void onHitEntity(EntityHitResult result) {
            this.entityHitCallbacks++;
            super.onHitEntity(result);
        }

        private void hitEntity(Entity target) {
            this.onHit(new EntityHitResult(target, Vec3.ZERO));
        }

        private boolean wasMarkedHurt() {
            return this.hurtMarked;
        }

        private boolean accepts(Entity target) {
            return this.canHitEntity(target);
        }
    }
}
