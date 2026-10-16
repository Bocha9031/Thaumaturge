package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class FluxGooFesterLifecyclePortTest {
    @Test
    void successfulFesterPollutesAuraBeforeEstablishingFoothold() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos pos = new BlockPos(4, 65, -3);
        FluidState gooState = FluxGooFluid.gooBlockState(8).getFluidState();
        AtomicBoolean polluted = new AtomicBoolean(false);

        when(level.getFluidState(pos)).thenReturn(gooState);
        when(level.getBlockState(pos.above())).thenReturn(Blocks.AIR.defaultBlockState());
        when(random.nextInt(50)).thenReturn(1, 0);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class);
                MockedStatic<TaintHelper> taint = mockStatic(TaintHelper.class);
                MockedStatic<TaintBlooms> blooms = mockStatic(TaintBlooms.class);
                MockedStatic<TaintBiomeManager> biomes = mockStatic(TaintBiomeManager.class)) {
            aura.when(() -> AuraHelper.polluteAura(level, pos, 1.0F, true)).thenAnswer(invocation -> {
                polluted.set(true);
                return null;
            });
            taint.when(() -> TaintHelper.establishFoothold(level, pos, 0.16F, 6))
                    .thenAnswer(invocation -> {
                        assertTrue(polluted.get());
                        return null;
                    });
            blooms.when(() -> TaintBlooms.isProtected(level, pos)).thenReturn(false);
            biomes.when(() -> TaintBiomeManager.isTainted(level, pos)).thenReturn(true);

            invokeLifecycle(goo, level, pos, gooState, random);

            assertTrue(polluted.get());
            aura.verify(() -> AuraHelper.polluteAura(level, pos, 1.0F, true));
            taint.verify(() -> TaintHelper.establishFoothold(level, pos, 0.16F, 6));
            verify(random, times(2)).nextInt(50);
            verify(random, never()).nextInt(4);
        }
    }

    @Test
    void protectedFesterCandidateHasNoPollutionOrFootholdEffects() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos pos = new BlockPos(-2, 70, 9);
        FluidState gooState = FluxGooFluid.gooBlockState(8).getFluidState();

        when(level.getFluidState(pos)).thenReturn(gooState);
        when(level.getBlockState(pos.above())).thenReturn(Blocks.AIR.defaultBlockState());
        when(random.nextInt(50)).thenReturn(1, 0);
        when(random.nextInt(4)).thenReturn(0);
        when(random.nextBoolean()).thenReturn(false);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class);
                MockedStatic<TaintHelper> taint = mockStatic(TaintHelper.class);
                MockedStatic<TaintBlooms> blooms = mockStatic(TaintBlooms.class);
                MockedStatic<PhysicalFluxAuraFloor> floor = mockStatic(PhysicalFluxAuraFloor.class)) {
            blooms.when(() -> TaintBlooms.isProtected(level, pos)).thenReturn(true);
            floor.when(PhysicalFluxAuraFloor::isEnabled).thenReturn(true);

            invokeLifecycle(goo, level, pos, gooState, random);

            aura.verifyNoInteractions();
            taint.verify(() -> TaintHelper.establishFoothold(level, pos, 0.16F, 6), never());
            verify(level).setBlock(pos, FluxGooFluid.gooBlockState(7), Block.UPDATE_CLIENTS);
            verify(random, times(2)).nextInt(50);
            verify(random).nextInt(4);
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
