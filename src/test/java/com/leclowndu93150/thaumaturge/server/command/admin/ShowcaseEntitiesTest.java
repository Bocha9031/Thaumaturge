package com.leclowndu93150.thaumaturge.server.command.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintacle;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
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
