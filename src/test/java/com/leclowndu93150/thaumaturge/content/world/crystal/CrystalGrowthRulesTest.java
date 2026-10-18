package com.leclowndu93150.thaumaturge.content.world.crystal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class CrystalGrowthRulesTest {
    @Test
    void sizeCeilingUsesPositionVariationAndGeneration() {
        BlockState defaultState = TTBlocks.CRYSTAL_AER.get().defaultBlockState();
        BlockPos pos = new BlockPos(17, 63, -29);
        int bonus = Math.floorMod(Mth.getSeed(pos), 3);

        for (int generation = 1; generation <= 4; generation++) {
            BlockState state = defaultState.setValue(BlockCrystal.GENERATION, generation);
            assertEquals(Math.min(3, 5 - generation + bonus), BlockCrystal.sizeCeiling(state, pos));
        }
    }

    @Test
    void spreadTargetSelectsEachAdjacentCellWithoutSelectingItsOwnPosition() {
        RandomSource random = mock(RandomSource.class);
        BlockPos origin = BlockPos.ZERO;
        Set<BlockPos> targets = new HashSet<>();

        for (int index = 0; index < 26; index++) {
            when(random.nextInt(26)).thenReturn(index);
            BlockPos target = BlockCrystal.neighbourWithin(origin, random);
            assertNotEquals(origin, target);
            assertTrue(Math.abs(target.getX()) <= 1);
            assertTrue(Math.abs(target.getY()) <= 1);
            assertTrue(Math.abs(target.getZ()) <= 1);
            targets.add(target);
        }

        assertEquals(26, targets.size());
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
