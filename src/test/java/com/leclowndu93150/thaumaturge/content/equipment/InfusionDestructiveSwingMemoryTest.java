package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class InfusionDestructiveSwingMemoryTest {
    @Test
    void rememberedFaceIsReturnedOnlyForMatchingBlockAndOnlyOnce() {
        DestructiveSwingMemory memory = new DestructiveSwingMemory();
        ResourceKey<Level> dimension = dimension("test");
        BlockPos pos = new BlockPos(3, 4, 5);
        memory.remember(dimension, pos, Direction.NORTH);

        assertEquals(Direction.NORTH, memory.takeFace(dimension, pos));
        assertNull(memory.takeFace(dimension, pos));
    }

    @Test
    void mismatchedDimensionOrPositionDiscardsRememberedFace() {
        DestructiveSwingMemory memory = new DestructiveSwingMemory();
        ResourceKey<Level> dimension = dimension("test");
        BlockPos pos = new BlockPos(3, 4, 5);
        memory.remember(dimension, pos, Direction.NORTH);

        assertNull(memory.takeFace(dimension("other"), pos));
        assertNull(memory.takeFace(dimension, pos));

        memory.remember(dimension, pos, Direction.SOUTH);
        assertNull(memory.takeFace(dimension, pos.offset(1, 0, 0)));
        assertNull(memory.takeFace(dimension, pos));
    }

    @Test
    void forgetClearsRememberedFace() {
        DestructiveSwingMemory memory = new DestructiveSwingMemory();
        ResourceKey<Level> dimension = dimension("test");
        BlockPos pos = new BlockPos(3, 4, 5);
        memory.remember(dimension, pos, Direction.NORTH);
        memory.forget();

        assertNull(memory.takeFace(dimension, pos));
    }

    private static ResourceKey<Level> dimension(String path) {
        return ResourceKey.create(Registries.DIMENSION, TTIds.rl(path));
    }
}
