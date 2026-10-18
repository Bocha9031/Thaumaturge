package com.leclowndu93150.thaumaturge.content.aura.node;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

final class NodeScatter {
    private NodeScatter() {}

    static BlockPos around(BlockPos origin, int reach, RandomSource random) {
        return origin.offset(offset(reach, random), offset(reach, random), offset(reach, random));
    }

    private static int offset(int reach, RandomSource random) {
        float fraction = random.nextFloat();
        int magnitude = Math.min(reach, (int) (fraction * fraction * (reach + 1)));
        return random.nextBoolean() ? magnitude : -magnitude;
    }
}
