package com.leclowndu93150.thaumaturge.server.command.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintacle;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSeed;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSeedPrime;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantCrescent;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantHammer;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantNova;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantSigil;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellCloud;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellProjectile;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.junit.jupiter.api.Test;

class ShowcaseEntitiesTest {
    @Test
    void syncedNameFreezesClientWithoutServerTags() {
        Entity entity = mock(Entity.class);
        when(entity.getTags()).thenReturn(Set.of());
        Component label = Component.literal("Specimen").withStyle(style -> style.withInsertion("thaumaturge_showcase"));
        when(entity.getCustomName()).thenReturn(label);
        entity.tickCount = 600;
        EntityTickEvent.Pre event = new EntityTickEvent.Pre(entity);

        ShowcaseEntities.onTick(event);

        assertTrue(event.isCanceled());
        assertEquals(1, entity.tickCount);
        verify(entity).setDeltaMovement(Vec3.ZERO);
    }

    @Test
    void frozenTaintacleIsPlacedAtItsFullyEmergedPose() {
        AbstractTaintacle taintacle = mock(AbstractTaintacle.class);
        when(taintacle.getBbHeight()).thenReturn(3.0F);

        assertEquals(30, ShowcaseEntities.displayTickCount(taintacle));
    }

    @Test
    void frozenGreaterPortalStartsAtItsFullyGrownScale() {
        EntityCultistPortalGreater portal = mock(EntityCultistPortalGreater.class);

        assertEquals(50, ShowcaseEntities.displayTickCount(portal));
    }

    @Test
    void frozenLesserPortalKeepsItsGrowthAnimationAtTheDisplayPose() {
        EntityCultistPortalLesser portal = mock(EntityCultistPortalLesser.class);
        when(portal.getTags()).thenReturn(Set.of("thaumaturge_showcase"));
        when(portal.getCustomName()).thenReturn(null);
        EntityTickEvent.Pre event = new EntityTickEvent.Pre(portal);

        ShowcaseEntities.onTick(event);

        assertEquals(50, portal.activeCounter);
        assertEquals(50, portal.tickCount);
        assertTrue(event.isCanceled());
    }

    @Test
    void taintSeedsAreRaisedToTheirDisplayHeights() {
        assertEquals(1.0, ShowcaseEntities.displayYOffset(mock(EntityTaintSeed.class)));
        assertEquals(3.0, ShowcaseEntities.displayYOffset(mock(EntityTaintSeedPrime.class)));
    }

    @Test
    void hierophantSpellsUseVisibleShowcaseAges() {
        assertEquals(4, ShowcaseEntities.displayAge(mock(EntityHierophantNova.class)));
        assertEquals(5, ShowcaseEntities.displayAge(mock(EntityHierophantHammer.class)));
        assertEquals(34, ShowcaseEntities.displayAge(mock(EntityHierophantSigil.class)));
        assertEquals(2, ShowcaseEntities.displayAge(mock(EntityHierophantCrescent.class)));
    }

    @Test
    void showcaseParticleCarriersTickOnlyOnClientForTheirVisibleEffects() {
        assertTrue(ShowcaseEntities.hasShowcaseParticleAnimation(mock(SpellProjectile.class)));
        assertTrue(ShowcaseEntities.hasShowcaseParticleAnimation(mock(SpellCloud.class)));
        assertFalse(ShowcaseEntities.hasShowcaseParticleAnimation(mock(Entity.class)));
    }

    @Test
    void ordinaryEntitiesStillTickAndTakeDamage() {
        Entity entity = mock(Entity.class);
        when(entity.getTags()).thenReturn(Set.of());
        when(entity.getCustomName()).thenReturn(Component.literal("Specimen"));
        EntityTickEvent.Pre tick = new EntityTickEvent.Pre(entity);
        EntityInvulnerabilityCheckEvent damage = new EntityInvulnerabilityCheckEvent(entity, null, false);

        ShowcaseEntities.onTick(tick);
        ShowcaseEntities.onInvulnerability(damage);

        assertFalse(tick.isCanceled());
        assertFalse(damage.isInvulnerable());
    }

    @Test
    void savedSpecimensRemainInvulnerableAndCannotDespawnInPeaceful() {
        Mob entity = mock(Mob.class);
        when(entity.getTags()).thenReturn(Set.of("thaumaturge_showcase"));
        EntityInvulnerabilityCheckEvent damage = new EntityInvulnerabilityCheckEvent(entity, null, false);
        MobDespawnEvent despawn = new MobDespawnEvent(entity, mock(ServerLevel.class));

        ShowcaseEntities.onInvulnerability(damage);
        ShowcaseEntities.onDespawn(despawn);

        assertTrue(damage.isInvulnerable());
        assertEquals(MobDespawnEvent.Result.DENY, despawn.getResult());
    }

    @Test
    void frozenLivingPosesFaceTheRowWithoutInterpolation() {
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getTags()).thenReturn(Set.of("thaumaturge_showcase"));
        when(entity.getYRot()).thenReturn(90.0F);
        entity.yHeadRot = 90.0F;
        entity.yBodyRot = 90.0F;

        ShowcaseEntities.onTick(new EntityTickEvent.Pre(entity));

        verify(entity).setYHeadRot(90.0F);
        verify(entity).setYBodyRot(90.0F);
        assertEquals(entity.yHeadRot, entity.yHeadRotO);
        assertEquals(entity.yBodyRot, entity.yBodyRotO);
    }
}
