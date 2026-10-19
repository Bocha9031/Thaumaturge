package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

final class ScatteredFlowersDecoratorTest {
    @Test
    void picksMostPopulatedColumnThenBreaksTiesByPositionRegardlessOfInputOrder() {
        ObjectArrayList<BlockPos> logs = new ObjectArrayList<>();
        logs.add(new BlockPos(5, 8, 1));
        logs.add(new BlockPos(-2, 7, 9));
        logs.add(new BlockPos(5, 4, 1));
        logs.add(new BlockPos(-2, 3, 9));
        logs.add(new BlockPos(8, 1, 0));
        ObjectArrayList<BlockPos> reversed = new ObjectArrayList<>();
        for (int index = logs.size() - 1; index >= 0; index--) {
            reversed.add(logs.get(index));
        }

        BlockPos expectedFoot = new BlockPos(-2, 3, 9);
        assertEquals(expectedFoot, ScatteredFlowersDecorator.findFoot(logs));
        assertEquals(expectedFoot, ScatteredFlowersDecorator.findFoot(reversed));
    }

    @Test
    void populatedColumnWinsOverCoordinateTieBreak() {
        ObjectArrayList<BlockPos> logs = new ObjectArrayList<>(new BlockPos[] {
            new BlockPos(-8, 0, 0), new BlockPos(3, 9, 0), new BlockPos(3, 2, 0), new BlockPos(3, 6, 0)
        });

        assertEquals(new BlockPos(3, 2, 0), ScatteredFlowersDecorator.findFoot(logs));
    }
}
