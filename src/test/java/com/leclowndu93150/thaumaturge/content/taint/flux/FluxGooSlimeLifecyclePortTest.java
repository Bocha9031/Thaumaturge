package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

class FluxGooSlimeLifecyclePortTest {
    @Test
    void failedSlimeCreationPreservesGooAndContinuesToDecay() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos pos = new BlockPos(4, 65, -3);
        FluidState gooState = FluxGooFluid.gooBlockState(3).getFluidState();

        when(level.enabledFeatures()).thenReturn(FeatureFlagSet.of());
        when(level.getFluidState(pos)).thenReturn(gooState);
        when(level.getBlockState(pos.above())).thenReturn(Blocks.AIR.defaultBlockState());
        when(random.nextInt(50)).thenReturn(0);
        when(random.nextInt(4)).thenReturn(0);
        when(random.nextBoolean()).thenReturn(false);

        assertFalse(TTEntities.THAUMIC_SLIME.get().isEnabled(FeatureFlagSet.of()));
        assertNull(TTEntities.THAUMIC_SLIME.get().create(level));
        try (MockedStatic<PhysicalFluxAuraFloor> floor = mockStatic(PhysicalFluxAuraFloor.class)) {
            floor.when(PhysicalFluxAuraFloor::isEnabled).thenReturn(true);

            invokeLifecycle(goo, level, pos, gooState, random);
        }

        verify(level).setBlock(pos, FluxGooFluid.gooBlockState(2), Block.UPDATE_CLIENTS);
        verify(level, never()).setBlock(eq(pos), eq(Blocks.AIR.defaultBlockState()), anyInt());
        verify(random).nextInt(50);
        verify(random).nextInt(4);
    }

    @Test
    void successfulCreationUsesSourcePoseBeforeClearingGoo() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos pos = new BlockPos(-2, 70, 9);
        FluidState gooState = FluxGooFluid.gooBlockState(3).getFluidState();
        when(level.enabledFeatures()).thenReturn(FeatureFlags.VANILLA_SET);
        when(level.getFluidState(pos)).thenReturn(gooState);
        when(level.getBlockState(pos.above())).thenReturn(Blocks.AIR.defaultBlockState());
        when(random.nextInt(50)).thenReturn(0);

        try (MockedConstruction<ThaumicSlime> slimes = mockConstruction(ThaumicSlime.class)) {
            invokeLifecycle(goo, level, pos, gooState, random);

            assertEquals(1, slimes.constructed().size());
            ThaumicSlime slime = slimes.constructed().get(0);
            InOrder order = inOrder(slime, level);
            order.verify(slime).setSize(1, true);
            order.verify(slime).moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
            order.verify(level).setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            order.verify(level).addFreshEntity(slime);
            order.verify(level)
                    .playSound(eq(null), eq(pos), eq(TTSounds.GORE.get()), eq(SoundSource.BLOCKS), eq(1.0F), eq(1.0F));
            verify(random, never()).nextInt(4);
        }
    }

    private static void invokeLifecycle(
            FluxGooFluid goo, ServerLevel level, BlockPos pos, FluidState state, RandomSource random) throws Exception {
        Method lifecycle = FluxGooFluid.class.getDeclaredMethod(
                "lifecycleTick", ServerLevel.class, BlockPos.class, FluidState.class, RandomSource.class);
        lifecycle.setAccessible(true);
        lifecycle.invoke(goo, level, pos, state, random);
    }
}
