package com.leclowndu93150.thaumaturge.api.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.junit.jupiter.api.Test;

class AreaPreviewerTest {
    private static final HitResult HIT = mock(HitResult.class);
    private static final List<BlockPos> POSITIONS = List.of(BlockPos.ZERO, BlockPos.ZERO.above());

    @Test
    void exposesThePreviewContract() {
        AreaPreviewer previewer = new TestPreviewer();
        ItemStack stack = ItemStack.EMPTY;
        Level level = mock(Level.class);
        Player player = mock(Player.class);

        assertSame(HIT, previewer.traceAnchor(stack, level, player));
        assertTrue(previewer.hidesBlockOutline(stack));
        assertEquals(POSITIONS, previewer.previewArea(stack, level, BlockPos.ZERO, Direction.UP, player));
        assertTrue(previewer.showsAxisArrows(stack, level, player, Direction.UP, Direction.Axis.X));
    }

    private static final class TestPreviewer implements AreaPreviewer {
        @Override
        public HitResult traceAnchor(ItemStack stack, Level level, LivingEntity holder) {
            return HIT;
        }

        @Override
        public boolean hidesBlockOutline(ItemStack stack) {
            return true;
        }

        @Override
        public List<BlockPos> previewArea(
                ItemStack stack, Level level, BlockPos anchor, Direction face, Player player) {
            return POSITIONS;
        }

        @Override
        public boolean showsAxisArrows(
                ItemStack stack, Level level, Player player, Direction face, Direction.Axis axis) {
            return true;
        }
    }
}
