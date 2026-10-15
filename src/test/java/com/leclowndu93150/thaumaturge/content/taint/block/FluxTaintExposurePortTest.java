package com.leclowndu93150.thaumaturge.content.taint.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.taint.effect.FluxTaintExposure;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintCreatureEvents;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintInfection;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class FluxTaintExposurePortTest {
    @Test
    void directExposurePreservesCallerDurationAndParticleFlags() {
        LivingEntity living = mock(LivingEntity.class);

        FluxTaintExposure.expose(living, 100, false, true);

        ArgumentCaptor<MobEffectInstance> effect = ArgumentCaptor.forClass(MobEffectInstance.class);
        verify(living).addEffect(effect.capture());
        assertEquals(TTMobEffects.FLUX_TAINT, effect.getValue().getEffect());
        assertEquals(100, effect.getValue().getDuration());
        assertEquals(0, effect.getValue().getAmplifier());
        assertFalse(effect.getValue().isAmbient());
        assertTrue(effect.getValue().isVisible());
        assertFalse(effect.getValue().showIcon());
    }

    @Test
    void fibreContactOnlyExposesAtTheFeetAndOnAChosenRoll() {
        BlockTaintFibre fibre = TTBlocks.TAINT_FIBRE.get();
        BlockState state = fibre.defaultBlockState();
        BlockPos feet = new BlockPos(5, 64, 0);
        LivingEntity living = mock(LivingEntity.class);
        EntityType<?> type = mock(EntityType.class);
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        when(level.isClientSide()).thenReturn(false);
        when(level.getRandom()).thenReturn(random);
        doReturn(type).when(living).getType();
        when(type.is(EntityTypeTags.UNDEAD)).thenReturn(false);

        when(living.blockPosition()).thenReturn(feet.above());
        fibre.entityInside(state, level, feet, living);
        verify(level, never()).getRandom();

        when(living.blockPosition()).thenReturn(feet);
        when(random.nextInt(750)).thenReturn(0);
        try (MockedStatic<MobTraits> traits = mockStatic(MobTraits.class)) {
            traits.when(() -> MobTraits.isTainted(living)).thenReturn(false);

            fibre.entityInside(state, level, feet, living);
        }

        ArgumentCaptor<MobEffectInstance> effect = ArgumentCaptor.forClass(MobEffectInstance.class);
        verify(living).addEffect(effect.capture());
        assertEquals(FluxTaintExposure.CONTACT_DURATION, effect.getValue().getDuration());
        assertTrue(effect.getValue().isAmbient());
        assertFalse(effect.getValue().isVisible());
        verify(random).nextInt(750);
    }

    @Test
    void exposureSkipsClientNonLivingAndImmuneEntitiesBeforeRolling() {
        Level clientLevel = mock(Level.class);
        Entity nonLiving = mock(Entity.class);
        when(clientLevel.isClientSide()).thenReturn(true);

        FluxTaintExposure.onStep(clientLevel, nonLiving, 1);

        verify(clientLevel, never()).getRandom();

        ServerLevel serverLevel = mock(ServerLevel.class);
        when(serverLevel.isClientSide()).thenReturn(false);
        FluxTaintExposure.onStep(serverLevel, nonLiving, 1);

        LivingEntity undead = mock(LivingEntity.class);
        EntityType<?> undeadType = mock(EntityType.class);
        doReturn(undeadType).when(undead).getType();
        when(undeadType.is(EntityTypeTags.UNDEAD)).thenReturn(true);
        LivingEntity tainted = mock(LivingEntity.class);

        try (MockedStatic<MobTraits> traits = mockStatic(MobTraits.class)) {
            traits.when(() -> MobTraits.isTainted(undead)).thenReturn(false);
            traits.when(() -> MobTraits.isTainted(tainted)).thenReturn(true);

            FluxTaintExposure.onStep(serverLevel, undead, 1);
            FluxTaintExposure.onStep(serverLevel, tainted, 1);
        }

        verify(clientLevel, never()).getRandom();
        verify(serverLevel, never()).getRandom();
        verify(undead, never()).addEffect(any(MobEffectInstance.class));
        verify(tainted, never()).addEffect(any(MobEffectInstance.class));
    }

    @Test
    void failedFibreContactRollDoesNotApplyFluxTaint() {
        BlockTaintFibre fibre = TTBlocks.TAINT_FIBRE.get();
        BlockState state = fibre.defaultBlockState();
        BlockPos feet = new BlockPos(5, 64, 0);
        LivingEntity living = mock(LivingEntity.class);
        EntityType<?> type = mock(EntityType.class);
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        when(level.isClientSide()).thenReturn(false);
        when(level.getRandom()).thenReturn(random);
        when(living.blockPosition()).thenReturn(feet);
        doReturn(type).when(living).getType();
        when(type.is(EntityTypeTags.UNDEAD)).thenReturn(false);
        when(random.nextInt(750)).thenReturn(1);

        try (MockedStatic<MobTraits> traits = mockStatic(MobTraits.class)) {
            traits.when(() -> MobTraits.isTainted(living)).thenReturn(false);

            fibre.entityInside(state, level, feet, living);
        }

        verify(random).nextInt(750);
        verify(living, never()).addEffect(any(MobEffectInstance.class));
    }

    @Test
    void taintedInfectionUsesUndeadTagRatherThanInvertedHealing() {
        ServerLevel level = mock(ServerLevel.class);
        LivingEntity victim = mock(LivingEntity.class);
        EntityType<?> victimType = mock(EntityType.class);
        LivingIncomingDamageEvent event = mock(LivingIncomingDamageEvent.class);
        RandomSource random = mock(RandomSource.class);
        when(event.getEntity()).thenReturn(victim);
        when(victim.level()).thenReturn(level);
        when(victim.getHealth()).thenReturn(1.0F);
        doReturn(victimType).when(victim).getType();
        when(victimType.is(EntityTypeTags.UNDEAD)).thenReturn(false);
        when(victim.isInvertedHealAndHarm()).thenReturn(true);
        when(victim.isAlive()).thenReturn(true);
        when(victim.hasEffect(TTMobEffects.FLUX_TAINT)).thenReturn(true);
        when(victim.getRandom()).thenReturn(random);
        when(random.nextBoolean()).thenReturn(true);

        try (MockedStatic<TaintInfection> infection = mockStatic(TaintInfection.class)) {
            TaintCreatureEvents.onIncomingDamage(event);

            infection.verify(() -> TaintInfection.tryInfect(level, victim));
        }
    }
}
