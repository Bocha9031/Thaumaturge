package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
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

class FluxGooLastQuantumEvaporationPortTest {
    @Test
    void lastQuantumClearsGooBeforeAuraPollution() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos pos = new BlockPos(4, 65, -3);
        FluidState gooState = FluxGooFluid.gooBlockState(1).getFluidState();
        AtomicBoolean cleared = new AtomicBoolean(false);

        stubLastQuantumDecay(level, random, pos, gooState);
        when(level.setBlock(eq(pos), eq(Blocks.AIR.defaultBlockState()), eq(Block.UPDATE_ALL)))
                .thenAnswer(invocation -> {
                    cleared.set(true);
                    return true;
                });

        try (MockedStatic<PhysicalFluxAuraFloor> floor = mockStatic(PhysicalFluxAuraFloor.class);
                MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            floor.when(PhysicalFluxAuraFloor::isEnabled).thenReturn(false);
            aura.when(() -> AuraHelper.polluteAura(level, pos, 1.0F, true)).thenAnswer(invocation -> {
                assertTrue(cleared.get());
                return null;
            });

            invokeLifecycle(goo, level, pos, gooState, random);

            assertTrue(cleared.get());
            verify(level).setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            aura.verify(() -> AuraHelper.polluteAura(level, pos, 1.0F, true));
        }
    }

    @Test
    void enabledAuraFloorSkipsPollutionWhenLastQuantumEvaporates() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos pos = new BlockPos(-2, 70, 9);
        FluidState gooState = FluxGooFluid.gooBlockState(1).getFluidState();

        stubLastQuantumDecay(level, random, pos, gooState);
        when(level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL))
                .thenReturn(true);

        try (MockedStatic<PhysicalFluxAuraFloor> floor = mockStatic(PhysicalFluxAuraFloor.class);
                MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            floor.when(PhysicalFluxAuraFloor::isEnabled).thenReturn(true);

            invokeLifecycle(goo, level, pos, gooState, random);

            verify(level).setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            aura.verifyNoInteractions();
        }
    }

    private static void stubLastQuantumDecay(
            ServerLevel level, RandomSource random, BlockPos pos, FluidState gooState) {
        when(level.getFluidState(pos)).thenReturn(gooState);
        when(level.getBlockState(pos.above())).thenReturn(Blocks.AIR.defaultBlockState());
        when(random.nextInt(4)).thenReturn(0);
        when(random.nextBoolean()).thenReturn(true);
    }

    private static void invokeLifecycle(
            FluxGooFluid goo, ServerLevel level, BlockPos pos, FluidState state, RandomSource random) throws Exception {
        Method lifecycle = FluxGooFluid.class.getDeclaredMethod(
                "lifecycleTick", ServerLevel.class, BlockPos.class, FluidState.class, RandomSource.class);
        lifecycle.setAccessible(true);
        lifecycle.invoke(goo, level, pos, state, random);
    }
}
