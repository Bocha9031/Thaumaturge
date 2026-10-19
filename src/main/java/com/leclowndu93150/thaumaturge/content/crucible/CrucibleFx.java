package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

final class CrucibleFx {
    private static final int BOIL_EFFECT_COUNT = 10;
    private static final int BUBBLE_ODDS = 6;
    private static final double CENTRE = 0.5;
    private static final double FROTH_SPREAD = 0.3;
    private static final double RIM_OUTSET = 0.04;
    private static final double BUBBLE_PIXEL = 1.0 / 16.0;
    private static final int BUBBLE_GRID_INSET = 3;
    private static final int BUBBLE_GRID_CELLS = 10;
    private static final double BUBBLE_SURFACE_LIFT = 0.02;
    private static final int NO_ASPECT_COLOR = 0xFFFFFF;
    private static final double BAMF_LIFT = 0.75;
    private static final float BAMF_RED = 0.5F;
    private static final float BAMF_GREEN = 0.1F;
    private static final float BAMF_BLUE = 0.6F;

    private CrucibleFx() {}

    static void froth(ServerLevel level, BlockPos pos, float surface, boolean overfilled) {
        RandomSource random = level.getRandom();
        Effects.crucibleFroth(level, frothPoint(pos, surface, random)).send();
        if (!overfilled) {
            return;
        }
        for (int pass = 0; pass < 2; pass++) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                Effects.crucibleFrothDown(level, rimPoint(pos, side, random)).send();
            }
        }
    }

    static void bubble(ServerLevel level, BlockPos pos, float surface, AspectList aspects) {
        RandomSource random = level.getRandom();
        if (aspects.isEmpty() || random.nextInt(BUBBLE_ODDS) != 0) {
            return;
        }
        int color = randomColor(aspects, random);
        Effects.crucibleBubble(level, bubblePoint(pos, surface, random))
                .color(red(color), green(color), blue(color))
                .send();
    }

    static void boilBurst(ServerLevel level, BlockPos pos, float surface, AspectList aspects, int heat) {
        RandomSource random = level.getRandom();
        Vec3 centre = new Vec3(pos.getX() + CENTRE, pos.getY() + surface, pos.getZ() + CENTRE);
        for (int i = 0; i < BOIL_EFFECT_COUNT; i++) {
            int color = aspects.isEmpty() ? NO_ASPECT_COLOR : randomColor(aspects, random);
            Effects.crucibleBoil(level, centre)
                    .color(red(color), green(color), blue(color))
                    .heat(heat)
                    .send();
        }
    }

    static void craftComplete(ServerLevel level, BlockPos pos) {
        Vec3 at = Vec3.atCenterOf(pos).add(0.0, BAMF_LIFT, 0.0);
        Effects.bamf(level, at)
                .color(BAMF_RED, BAMF_GREEN, BAMF_BLUE)
                .withSound()
                .fancy()
                .side(Direction.UP)
                .send();
    }

    static Vec3 frothPoint(BlockPos pos, float surface, RandomSource random) {
        return new Vec3(
                pos.getX() + CENTRE + spread(random, FROTH_SPREAD),
                pos.getY() + surface,
                pos.getZ() + CENTRE + spread(random, FROTH_SPREAD));
    }

    static Vec3 rimPoint(BlockPos pos, Direction side, RandomSource random) {
        Direction along = side.getClockWise();
        double out = CENTRE + RIM_OUTSET;
        double slide = random.nextDouble() - CENTRE;
        double x = pos.getX() + CENTRE + side.getStepX() * out + along.getStepX() * slide;
        double z = pos.getZ() + CENTRE + side.getStepZ() * out + along.getStepZ() * slide;
        return new Vec3(x, pos.getY() + 1.0, z);
    }

    static Vec3 bubblePoint(BlockPos pos, float surface, RandomSource random) {
        return new Vec3(
                pos.getX() + bubbleGridCoordinate(random),
                pos.getY() + surface + BUBBLE_SURFACE_LIFT,
                pos.getZ() + bubbleGridCoordinate(random));
    }

    private static double bubbleGridCoordinate(RandomSource random) {
        return (BUBBLE_GRID_INSET + random.nextInt(BUBBLE_GRID_CELLS) + CENTRE) * BUBBLE_PIXEL;
    }

    private static double spread(RandomSource random, double reach) {
        return (random.nextDouble() * 2.0 - 1.0) * reach;
    }

    private static int randomColor(AspectList aspects, RandomSource random) {
        return aspects.entries()
                .get(random.nextInt(aspects.size()))
                .aspect()
                .value()
                .color();
    }

    private static float red(int color) {
        return ARGB32.red(color) / 255.0F;
    }

    private static float green(int color) {
        return ARGB32.green(color) / 255.0F;
    }

    private static float blue(int color) {
        return ARGB32.blue(color) / 255.0F;
    }
}
