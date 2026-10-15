package com.leclowndu93150.thaumaturge.content.warp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.entity.EntityMindSpider;
import com.leclowndu93150.thaumaturge.content.warp.spawn.SpawnSpots;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

class WarpSpawnParityTest {
    @Test
    void ringCellRejectsCenterAndUsesConditionalAxisDraws() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(3)).thenReturn(0, 0, 0, 1, 0, 0);
        when(random.nextInt(18)).thenReturn(0);
        when(random.nextBoolean()).thenReturn(true);
        BlockPos origin = new BlockPos(10, 20, 30);

        BlockPos result = SpawnSpots.ringCell(origin, random);

        assertEquals(origin.offset(7, 0, 0), result);
        verify(random, times(6)).nextInt(3);
        verify(random).nextInt(18);
        verify(random).nextBoolean();
    }

    @Test
    void ringCellKeepsSignedOffsetsAtInclusiveDistanceBounds() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(3)).thenReturn(1, 1, 1);
        when(random.nextInt(18)).thenReturn(0, 17, 5);
        when(random.nextBoolean()).thenReturn(false, true, false);
        BlockPos origin = new BlockPos(10, 20, 30);

        BlockPos result = SpawnSpots.ringCell(origin, random);

        assertEquals(origin.offset(-7, 24, -12), result);
        verify(random, times(3)).nextInt(3);
        verify(random, times(3)).nextInt(18);
        verify(random, times(3)).nextBoolean();
    }

    @Test
    void deadlyGazeExcludesSpectatorsFromItsTargetQuery() {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        MobEffectInstance gaze = mock(MobEffectInstance.class);
        when(player.level()).thenReturn(level);
        when(player.getEffect(TTMobEffects.DEATH_GAZE)).thenReturn(gaze);
        when(player.getBoundingBox()).thenReturn(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
        when(gaze.getAmplifier()).thenReturn(0);
        when(level.getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any()))
                .thenReturn(List.of());

        WarpEvents.checkDeathGaze(player);

        verify(level).getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), same(EntitySelector.NO_SPECTATORS));
    }

    @Test
    void spiderSummonCreatesOnceAcrossPlacementRetries() {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        RandomSource random = mock(RandomSource.class);
        BlockState floor = mock(BlockState.class);
        AABB bounds = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        when(player.level()).thenReturn(level);
        when(player.getRandom()).thenReturn(random);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.VANILLA_SET);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(floor);
        when(floor.isCollisionShapeFullBlock(eq(level), any(BlockPos.class))).thenReturn(true);
        when(level.noCollision(any(Entity.class))).thenReturn(false, true);
        when(level.containsAnyLiquid(bounds)).thenReturn(false);

        try (MockedConstruction<EntityMindSpider> spiders = mockConstruction(
                        EntityMindSpider.class,
                        (spider, context) -> when(spider.getBoundingBox()).thenReturn(bounds));
                MockedStatic<WarpManager> warpManager = mockStatic(WarpManager.class)) {
            WarpEvents.suddenlySpiders(player, 1, true);

            assertEquals(1, spiders.constructed().size());
            verify(spiders.constructed().get(0), times(2)).moveTo(0.0, 0.0, 0.0, 0.0F, 0.0F);
        }
    }
}
