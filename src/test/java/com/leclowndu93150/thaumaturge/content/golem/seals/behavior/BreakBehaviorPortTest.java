package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class BreakBehaviorPortTest {
    @Test
    void matchesBlockCloneAgainstWhitelist() {
        assertTrue(isWorkable(new ItemStack(Items.STONE), List.of(new ItemStack(Items.STONE)), false, false));
        assertFalse(isWorkable(new ItemStack(Items.DIRT), List.of(new ItemStack(Items.STONE)), false, false));
    }

    @Test
    void nonMatchingBlockPassesBlacklistAndLiquidsAreNeverWorkable() {
        assertTrue(isWorkable(new ItemStack(Items.DIRT), List.of(new ItemStack(Items.STONE)), true, false));
        assertFalse(isWorkable(new ItemStack(Items.STONE), List.of(), false, true));
    }

    private static boolean isWorkable(
            ItemStack droppedForm, List<ItemStack> filterStacks, boolean blacklist, boolean liquid) {
        Level level = mock(Level.class);
        BlockPos pos = BlockPos.ZERO;
        Block block = mock(Block.class);
        BlockState state = mock(BlockState.class);
        when(state.getBlock()).thenReturn(block);
        when(state.isAir()).thenReturn(false);
        when(state.liquid()).thenReturn(liquid);
        when(state.getDestroySpeed(level, pos)).thenReturn(1.0F);
        when(block.getCloneItemStack(level, pos, state)).thenReturn(droppedForm);
        when(level.getBlockState(pos)).thenReturn(state);

        ISealFilter filter = mock(ISealFilter.class);
        when(filter.stacks()).thenReturn(filterStacks);
        when(filter.isBlacklist()).thenReturn(blacklist);
        ISealEntity seal = mock(ISealEntity.class);
        when(seal.filter()).thenReturn(Optional.of(filter));

        return new BreakBehavior().isWorkable(level, seal, pos);
    }
}
