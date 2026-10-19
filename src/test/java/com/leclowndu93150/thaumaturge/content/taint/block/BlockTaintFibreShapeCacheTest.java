package com.leclowndu93150.thaumaturge.content.taint.block;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

class BlockTaintFibreShapeCacheTest {
    @Test
    void outlineShapeIsReusedForRepeatedStateQueries() {
        BlockTaintFibre fibre = TTBlocks.TAINT_FIBRE.get();
        BlockState state =
                fibre.defaultBlockState().setValue(BlockTaintFibre.NORTH, true).setValue(BlockTaintFibre.GROWTH3, true);

        VoxelShape first = fibre.getShape(state, null, BlockPos.ZERO, CollisionContext.empty());
        VoxelShape second = fibre.getShape(state, null, BlockPos.ZERO, CollisionContext.empty());

        assertNotNull(first);
        assertSame(first, second);
    }
}
