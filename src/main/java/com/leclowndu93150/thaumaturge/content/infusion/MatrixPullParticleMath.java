package com.leclowndu93150.thaumaturge.content.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

final class MatrixPullParticleMath {
    static final int SOURCE_PARTICLE_COUNT = 4;
    static final float SPARKLE_GREEN = 0.2F;
    static final double CRUMB_ITEM_KICK = 0.03;

    private static final float SPARKLE_PURPLE_RED = 0.4F;
    private static final float SPARKLE_VIOLET_RED = 0.6F;
    private static final float SPARKLE_BLUE_MIN = 0.6F;
    private static final float SPARKLE_BLUE_SPAN = 0.3F;
    private static final double CRUMB_BOX_OFFSET_XZ = 0.4;
    private static final double CRUMB_BOX_OFFSET_Y = 1.23;
    private static final double CRUMB_BOX_SPAN = 0.2;

    private MatrixPullParticleMath() {}

    static Vec3 nonBlockItemStart(BlockPos source, double xSample, double ySample, double zSample) {
        return new Vec3(
                source.getX() + CRUMB_BOX_OFFSET_XZ + CRUMB_BOX_SPAN * xSample,
                source.getY() + CRUMB_BOX_OFFSET_Y + CRUMB_BOX_SPAN * ySample,
                source.getZ() + CRUMB_BOX_OFFSET_XZ + CRUMB_BOX_SPAN * zSample);
    }

    static float sparkleRed(float sample) {
        return Mth.lerp(sample, SPARKLE_PURPLE_RED, SPARKLE_VIOLET_RED);
    }

    static float sparkleBlue(float sample) {
        return SPARKLE_BLUE_MIN + sample * SPARKLE_BLUE_SPAN;
    }
}
