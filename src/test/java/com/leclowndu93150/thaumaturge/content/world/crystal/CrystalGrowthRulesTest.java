package com.leclowndu93150.thaumaturge.content.world.crystal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class CrystalGrowthRulesTest {
    @Test
    void sizeCeilingUsesPositionVariationAndGeneration() {
        BlockState defaultState = TTBlocks.CRYSTAL_AER.get().defaultBlockState();
        BlockPos pos = new BlockPos(17, 63, -29);
        int bonus = (int) Math.floorMod(pos.asLong(), 3L);

        for (int generation = 1; generation <= 4; generation++) {
            BlockState state = defaultState.setValue(BlockCrystal.GENERATION, generation);
            assertEquals(Math.min(3, 5 - generation + bonus), BlockCrystal.sizeCeiling(state, pos));
        }
    }

    @Test
    void spreadTargetSelectsEachCellInItsLocalCubeIncludingItsOwnPosition() {
        RandomSource random = mock(RandomSource.class);
        BlockPos origin = BlockPos.ZERO;
        Set<BlockPos> targets = new HashSet<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    doReturn(dx + 1, dy + 1, dz + 1).when(random).nextInt(3);
                    BlockPos target = BlockCrystal.neighbourWithin(origin, random);
                    assertEquals(new BlockPos(dx, dy, dz), target);
                    targets.add(target);
                }
            }
        }

        assertEquals(27, targets.size());
        assertTrue(targets.contains(origin));
    }

    @Test
    void offspringUsuallyAdvancesButCanRetainItsGeneration() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(6)).thenReturn(0, 1, 1);

        assertEquals(3, BlockCrystal.childGeneration(3, random));
        assertEquals(4, BlockCrystal.childGeneration(3, random));
        assertEquals(4, BlockCrystal.childGeneration(4, random));
    }
}
