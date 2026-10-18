package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class ManaPodFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SURFACE_BELOW = 32;
    private static final int SURFACE_ABOVE = 16;
    private static final int CAVE_RADIUS = 8;
    private static final int WANDER_RADIUS = 3;
    private static final int YOUNGEST_START_STAGE = 2;
    private static final int OLDEST_START_STAGE = 6;

    public ManaPodFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        boolean cave = level.getBiome(context.origin()).is(TTBiomes.MAGICAL_FOREST_CAVES);
        BlockPos origin = context.origin();
        int anchor = cave
                ? origin.getY()
                : level.getHeight(Heightmap.Types.MOTION_BLOCKING, origin.getX(), origin.getZ()) - 1;
        int below = cave ? CAVE_RADIUS : SURFACE_BELOW;
        int above = cave ? CAVE_RADIUS : SURFACE_ABOVE;
        int bottom = anchor - below;
        int top = anchor + above;
        bottom = Math.max(level.getMinBuildHeight() + 1, bottom);
        top = Math.min(level.getMaxBuildHeight() - 1, top);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(origin.getX(), bottom, origin.getZ());
        while (cursor.getY() <= top) {
            if (!hangsOverAir(level, cursor)) {
                cursor.setX(origin.getX() + wanderOffset(random));
                cursor.setZ(origin.getZ() + wanderOffset(random));
            } else if (BlockManaPod.canGrowAt(level, cursor)) {
                int stage = random.nextIntBetweenInclusive(YOUNGEST_START_STAGE, OLDEST_START_STAGE);
                level.setBlock(
                        cursor,
                        BlockManaPod.placementState(level, cursor).setValue(BlockManaPod.AGE, stage),
                        Block.UPDATE_CLIENTS);
                if (level.getBlockEntity(cursor) instanceof BlockEntityManaPod pod) {
                    pod.rollSpawnAspect(level, random);
                }
                return true;
            }
            cursor.setY(cursor.getY() + 1);
        }
        return false;
    }

    private static int wanderOffset(RandomSource random) {
        return random.nextInt(WANDER_RADIUS + 1) - random.nextInt(WANDER_RADIUS + 1);
    }

    private static boolean hangsOverAir(WorldGenLevel level, BlockPos pos) {
        return level.isEmptyBlock(pos) && level.isEmptyBlock(pos.below());
    }
}
