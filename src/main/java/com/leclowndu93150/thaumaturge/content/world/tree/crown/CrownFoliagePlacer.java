package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import com.leclowndu93150.thaumaturge.content.world.tree.TreeLeafUpdater;
import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public final class CrownFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<CrownFoliagePlacer> CODEC =
            RecordCodecBuilder.mapCodec(instance -> foliagePlacerParts(instance)
                    .and(Codec.BOOL.fieldOf("absorb_foreign_leaves").forGetter(placer -> placer.absorbForeignLeaves))
                    .apply(instance, CrownFoliagePlacer::new));

    private static final int CLUSTER_LAYERS = 4;
    private static final int LAST_LAYER = CLUSTER_LAYERS - 1;
    private static final int SQUARE_RADIUS = 2;
    private static final int EDGE_REACH = 1;

    private final boolean absorbForeignLeaves;

    public CrownFoliagePlacer(IntProvider radius, IntProvider offset, boolean absorbForeignLeaves) {
        super(radius, offset);
        this.absorbForeignLeaves = absorbForeignLeaves;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return TTTreePlacers.CROWN_FOLIAGE.get();
    }

    @Override
    protected void createFoliage(
            LevelSimulatedReader reader,
            FoliagePlacer.FoliageSetter foliageSetter,
            RandomSource random,
            TreeConfiguration config,
            int treeHeight,
            FoliagePlacer.FoliageAttachment foliageAttachment,
            int foliageHeight,
            int leafRadius,
            int offset) {
        if (!(reader instanceof WorldGenLevel level)) {
            return;
        }
        BlockPos anchor = foliageAttachment.pos();
        BlockState leaves = config.foliageProvider.getState(random, anchor);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int layer = 0; layer < CLUSTER_LAYERS; layer++) {
            boolean plus = layer == 0 || layer == LAST_LAYER;
            for (int dx = -SQUARE_RADIUS; dx <= SQUARE_RADIUS; dx++) {
                for (int dz = -SQUARE_RADIUS; dz <= SQUARE_RADIUS; dz++) {
                    if (inLayer(plus, dx, dz)) {
                        cursor.setWithOffset(anchor, dx, layer, dz);
                        placeLeaf(level, foliageSetter, cursor, leaves);
                    }
                }
            }
        }
    }

    static boolean inLayer(boolean plus, int dx, int dz) {
        int absX = Math.abs(dx);
        int absZ = Math.abs(dz);
        if (plus) {
            return absX + absZ <= EDGE_REACH;
        }
        return !(absX == SQUARE_RADIUS && absZ == SQUARE_RADIUS);
    }

    private void placeLeaf(
            WorldGenLevel level, FoliagePlacer.FoliageSetter foliageSetter, BlockPos pos, BlockState leaves) {
        if (level.isOutsideBuildHeight(pos)) {
            return;
        }
        BlockState present = level.getBlockState(pos);
        if (present.isAir()) {
            foliageSetter.set(pos, leaves);
        } else if (absorbForeignLeaves && present.is(BlockTags.LEAVES) && !present.is(leaves.getBlock())) {
            foliageSetter.set(pos, TreeLeafUpdater.carryDistance(leaves, present));
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeConfiguration config) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(
            RandomSource random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk) {
        return false;
    }
}
