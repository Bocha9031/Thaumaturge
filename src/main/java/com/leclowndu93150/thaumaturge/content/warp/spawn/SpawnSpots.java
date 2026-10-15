package com.leclowndu93150.thaumaturge.content.warp.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class SpawnSpots {
    private static final int STILL_AXIS_ODDS = 3;
    private static final int NEAREST_SHIFT = 7;
    private static final int FARTHEST_SHIFT = 24;

    private SpawnSpots() {}

    public static BlockPos ringCell(BlockPos origin, RandomSource random) {
        int dx;
        int dy;
        int dz;
        do {
            dx = displacement(random);
            dy = displacement(random);
            dz = displacement(random);
        } while (dx == 0 && dy == 0 && dz == 0);
        return origin.offset(dx, dy, dz);
    }

    private static int displacement(RandomSource random) {
        if (random.nextInt(STILL_AXIS_ODDS) == 0) {
            return 0;
        }
        int distance = Mth.nextInt(random, NEAREST_SHIFT, FARTHEST_SHIFT);
        return random.nextBoolean() ? distance : -distance;
    }
}
