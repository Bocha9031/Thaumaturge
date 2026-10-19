package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.world.tree.TreeLeafUpdater;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public final class SilverwoodTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<SilverwoodTrunkPlacer> CODEC =
            RecordCodecBuilder.mapCodec(instance -> trunkPlacerParts(instance)
                    .and(Codec.BOOL.fieldOf("grow_nodes").forGetter(placer -> placer.growNodes))
                    .and(Codec.BOOL.fieldOf("keep_apart").forGetter(placer -> placer.keepApart))
                    .apply(instance, SilverwoodTrunkPlacer::new));

    private static final int PLACE_FLAGS = Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE;
    private static final int CROWN_DROP = 5;
    private static final int CORE_DROP = 3;
    private static final int CROWN_MIN_RISE = 3;
    private static final int CROWN_RISE_SPREAD = 2;
    private static final int CROWN_SCAN_RADIUS = 5;
    private static final int CROWN_THRESHOLD_BASE = 10;
    private static final int CROWN_THRESHOLD_SPREAD = 8;
    private static final int NARROW_ROOM_RADIUS = 1;
    private static final int WIDE_ROOM_RADIUS = 3;
    private static final int WIDE_ROOM_LAYERS = 3;
    private static final int BOUGH_DROP = 4;
    private static final int SIDE_LOG_REACH = 2;
    private static final int FLARE_STACK_ODDS = 3;
    private static final int BOUGH_DROOP_ODDS = 3;
    private static final double FIRST_NODE_ODDS = 1.5;
    private static final double NODE_ODDS_STEP = 1.0;
    private static final int KEEP_APART_RADIUS = 12;
    private static final int KEEP_APART_REACH = 8;
    private static final int CROWDING_RADIUS = 5;
    private static final int CROWDING_BELOW_ORIGIN = 1;
    private static final int CROWDING_ABOVE_TOP = 5;
    private static final int CROWDING_CANOPY_DEPTH = 5;
    private static final int CROWDING_TRUNK_RADIUS_SQUARED = 4;

    private final boolean growNodes;
    private final boolean keepApart;

    public SilverwoodTrunkPlacer(
            int baseHeight, int heightRandA, int heightRandB, boolean growNodes, boolean keepApart) {
        super(baseHeight, heightRandA, heightRandB);
        this.growNodes = growNodes;
        this.keepApart = keepApart;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TTTreePlacers.SILVERWOOD_TRUNK.get();
    }

    @Override
    public boolean isFree(LevelSimulatedReader reader, BlockPos pos) {
        return reader instanceof WorldGenLevel level && level.isStateAtPosition(pos, SilverwoodTrunkPlacer::isRoom);
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
            LevelSimulatedReader reader,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            int treeHeight,
            BlockPos origin,
            TreeConfiguration config) {
        if (!(reader instanceof WorldGenLevel level)) {
            return List.of();
        }
        int rise = CROWN_MIN_RISE + random.nextInt(CROWN_RISE_SPREAD + 1);
        if (!fitsBuildHeight(level, origin, treeHeight, rise)
                || !standsOnSoil(level, origin)
                || !level.getFluidState(origin).isEmpty()
                || !hasRoom(level, origin, treeHeight)
                || (keepApart
                        && (nearAnotherSilverwood(level, origin, treeHeight)
                                || crowdedByOtherTrees(level, origin, treeHeight)))) {
            return List.of();
        }
        List<FoliagePlacer.FoliageAttachment> crown = growCrown(level, random, config, origin, treeHeight, rise);
        raiseTrunk(level, trunkSetter, random, config, origin, treeHeight);
        raiseCore(level, trunkSetter, random, config, origin, treeHeight);
        spreadRootFlare(level, trunkSetter, random, config, origin);
        spreadBoughs(level, trunkSetter, random, config, origin.above(treeHeight - BOUGH_DROP));
        return crown;
    }

    private static boolean isRoom(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced();
    }

    private static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    private static boolean fitsBuildHeight(WorldGenLevel level, BlockPos origin, int treeHeight, int rise) {
        return !level.isOutsideBuildHeight(origin.getY() - 1)
                && !level.isOutsideBuildHeight(origin.getY() + treeHeight + rise);
    }

    private static boolean standsOnSoil(WorldGenLevel level, BlockPos origin) {
        return level.isStateAtPosition(origin.below(), SilverwoodTrunkPlacer::isSoil);
    }

    private static boolean hasRoom(WorldGenLevel level, BlockPos origin, int treeHeight) {
        int highest = treeHeight + 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy <= highest; dy++) {
            int radius = dy > highest - WIDE_ROOM_LAYERS ? WIDE_ROOM_RADIUS : NARROW_ROOM_RADIUS;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.setWithOffset(origin, dx, dy, dz);
                    if (!isRoom(level.getBlockState(cursor))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean nearAnotherSilverwood(WorldGenLevel level, BlockPos origin, int treeHeight) {
        Block silverwoodLog = TTBlocks.LOG_SILVERWOOD.get();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -KEEP_APART_REACH; dy <= treeHeight + KEEP_APART_REACH; dy++) {
            for (int dx = -KEEP_APART_RADIUS; dx <= KEEP_APART_RADIUS; dx++) {
                for (int dz = -KEEP_APART_RADIUS; dz <= KEEP_APART_RADIUS; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    cursor.setWithOffset(origin, dx, dy, dz);
                    if (level.getBlockState(cursor).is(silverwoodLog)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean crowdedByOtherTrees(WorldGenLevel level, BlockPos origin, int treeHeight) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -CROWDING_BELOW_ORIGIN; dy <= treeHeight + CROWDING_ABOVE_TOP; dy++) {
            for (int dx = -CROWDING_RADIUS; dx <= CROWDING_RADIUS; dx++) {
                for (int dz = -CROWDING_RADIUS; dz <= CROWDING_RADIUS; dz++) {
                    if (!isCrowdingZone(dx, dy, dz, treeHeight)) {
                        continue;
                    }
                    cursor.setWithOffset(origin, dx, dy, dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean isCrowdingZone(int dx, int dy, int dz, int treeHeight) {
        if (dy < -CROWDING_BELOW_ORIGIN || dy > treeHeight + CROWDING_ABOVE_TOP) {
            return false;
        }
        return dy >= treeHeight - CROWDING_CANOPY_DEPTH || dx * dx + dz * dz <= CROWDING_TRUNK_RADIUS_SQUARED;
    }

    private static List<FoliagePlacer.FoliageAttachment> growCrown(
            WorldGenLevel level,
            RandomSource random,
            TreeConfiguration config,
            BlockPos origin,
            int treeHeight,
            int rise) {
        List<FoliagePlacer.FoliageAttachment> leaves = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int fromTop = -CROWN_DROP; fromTop <= rise; fromTop++) {
            int vertical = verticalOffset(fromTop);
            for (int dx = -CROWN_SCAN_RADIUS; dx <= CROWN_SCAN_RADIUS; dx++) {
                for (int dz = -CROWN_SCAN_RADIUS; dz <= CROWN_SCAN_RADIUS; dz++) {
                    int threshold = CROWN_THRESHOLD_BASE + random.nextInt(CROWN_THRESHOLD_SPREAD);
                    if (!fitsCrown(dx, vertical, dz, threshold)) {
                        continue;
                    }
                    cursor.setWithOffset(origin, dx, treeHeight + fromTop, dz);
                    BlockState leaf = config.foliageProvider.getState(random, cursor);
                    BlockState replacement = leafReplacement(level.getBlockState(cursor), leaf);
                    if (replacement != null) {
                        level.setBlock(cursor, replacement, PLACE_FLAGS);
                        leaves.add(new FoliagePlacer.FoliageAttachment(cursor.immutable(), 0, false));
                    }
                }
            }
        }
        return leaves;
    }

    static int verticalOffset(int fromTop) {
        if (fromTop > 0) {
            return fromTop;
        }
        if (fromTop < -CORE_DROP) {
            return -CORE_DROP - fromTop;
        }
        return 0;
    }

    static boolean fitsCrown(int dx, int vertical, int dz, int threshold) {
        return dx * dx + vertical * vertical + dz * dz < threshold;
    }

    static BlockState leafReplacement(BlockState existing, BlockState leaf) {
        if (existing.is(leaf.getBlock())) {
            return null;
        }
        if (existing.is(BlockTags.LEAVES) || existing.getBlock() instanceof LeavesBlock) {
            return TreeLeafUpdater.carryDistance(leaf, existing);
        }
        if (existing.isAir() || existing.canBeReplaced()) {
            return leaf;
        }
        return null;
    }

    private static void raiseTrunk(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos origin,
            int treeHeight) {
        setLog(level, trunkSetter, random, config, origin, Direction.Axis.Y);
        for (int dy = 0; dy < treeHeight; dy++) {
            BlockPos layer = origin.above(dy);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                setLog(level, trunkSetter, random, config, layer.relative(side), Direction.Axis.Y);
            }
        }
    }

    private void raiseCore(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos origin,
            int treeHeight) {
        int nodesGrown = 0;
        boolean previousWasNode = false;
        for (int dy = 1; dy < treeHeight; dy++) {
            BlockPos pos = origin.above(dy);
            if (growNodes && !previousWasNode && rollsNode(random, treeHeight, nodesGrown)) {
                previousWasNode = plantNode(level, trunkSetter, random, config, pos);
                if (previousWasNode) {
                    nodesGrown++;
                }
            } else {
                previousWasNode = false;
                setLog(level, trunkSetter, random, config, pos, Direction.Axis.Y);
            }
        }
        setLog(level, trunkSetter, random, config, origin.above(treeHeight), Direction.Axis.Y);
    }

    private static boolean rollsNode(RandomSource random, int treeHeight, int nodesGrown) {
        int odds = Math.max(1, Mth.floor((FIRST_NODE_ODDS + nodesGrown * NODE_ODDS_STEP) * treeHeight));
        return random.nextInt(odds) == 0;
    }

    private static boolean plantNode(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos pos) {
        if (!level.isStateAtPosition(pos, SilverwoodTrunkPlacer::isRoom)) {
            return false;
        }
        trunkSetter.accept(pos, TTBlocks.SILVERWOOD_NODE_LOG.get().defaultBlockState());
        if (NodeGenerator.createRandomNodeAt(
                level,
                pos,
                random,
                true,
                false,
                false,
                NodeGenerator.DEFAULT_SPECIAL_RARITY,
                NodeGenerator.DEFAULT_BASE_AURA)) {
            return true;
        }
        trunkSetter.accept(pos, logState(random, config, pos, Direction.Axis.Y));
        return false;
    }

    private static void spreadRootFlare(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos origin) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos diagonal = origin.relative(side).relative(side.getClockWise());
            setLog(level, trunkSetter, random, config, diagonal, Direction.Axis.Y);
            if (random.nextInt(FLARE_STACK_ODDS) != 0) {
                setLog(level, trunkSetter, random, config, diagonal.above(), Direction.Axis.Y);
            }
            BlockPos root = origin.relative(side, SIDE_LOG_REACH);
            setLog(level, trunkSetter, random, config, root, side.getAxis());
            BlockPos anchor = root.below();
            if (level.isStateAtPosition(anchor, SilverwoodTrunkPlacer::isRoom)) {
                trunkSetter.accept(anchor, logState(random, config, anchor, Direction.Axis.Y));
            }
        }
    }

    private static void spreadBoughs(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos centre) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos diagonal = centre.relative(side).relative(side.getClockWise());
            setLog(level, trunkSetter, random, config, diagonal, Direction.Axis.Y);
            if (random.nextInt(BOUGH_DROOP_ODDS) == 0) {
                setLog(level, trunkSetter, random, config, diagonal.below(), Direction.Axis.Y);
            }
            setLog(level, trunkSetter, random, config, centre.relative(side, SIDE_LOG_REACH), side.getAxis());
        }
    }

    private static void setLog(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> trunkSetter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos pos,
            Direction.Axis axis) {
        if (level.isStateAtPosition(pos, SilverwoodTrunkPlacer::isRoom)) {
            trunkSetter.accept(pos, logState(random, config, pos, axis));
        }
    }

    private static BlockState logState(
            RandomSource random, TreeConfiguration config, BlockPos pos, Direction.Axis axis) {
        return config.trunkProvider.getState(random, pos).trySetValue(RotatedPillarBlock.AXIS, axis);
    }
}
