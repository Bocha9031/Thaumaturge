package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.content.taint.spread.FibreGrowth;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintFrontier;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSeedRegistry;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSprouting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class TaintHelper {
    private static final double SEED_VALIDATION_RANGE = 1.0;
    private static final double SEED_EDGE_RATIO = 0.8;

    private TaintHelper() {}

    public static double spreadArea() {
        return ThaumaturgeCommonConfig.TAINT_SPREAD_AREA.get();
    }

    public static double fringeDistance() {
        return spreadArea() * SEED_EDGE_RATIO;
    }

    public static void registerSeedAnchor(ServerLevel level, BlockPos pos) {
        TaintSeedRegistry.get(level).addSeed(pos);
    }

    public static void unregisterSeedAnchor(ServerLevel level, BlockPos pos) {
        TaintSeedRegistry.get(level).removeSeed(pos);
    }

    public static boolean isWithinSeedInfluence(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        double area = spreadArea() * spreadArea();
        TaintSeedRegistry registry = TaintSeedRegistry.get(serverLevel);
        List<BlockPos> stale = null;
        boolean found = false;
        for (BlockPos seed : registry.all()) {
            if (seed.distSqr(pos) > area || !serverLevel.areEntitiesLoaded(ChunkPos.asLong(seed))) {
                continue;
            }
            if (serverLevel
                    .getEntitiesOfClass(AbstractTaintSeed.class, new AABB(seed).inflate(SEED_VALIDATION_RANGE))
                    .isEmpty()) {
                if (stale == null) {
                    stale = new ArrayList<>();
                }
                stale.add(seed);
                continue;
            }
            found = true;
            break;
        }
        if (stale != null) {
            for (BlockPos seed : stale) {
                registry.removeSeed(seed);
            }
        }
        return found;
    }

    public static boolean isEcologicallySustained(ServerLevel level, BlockPos pos) {
        return TaintBiomeManager.isTainted(level, pos) || isWithinSeedInfluence(level, pos);
    }

    public static boolean isOnSeedFringe(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        double area = spreadArea() * spreadArea();
        double fringe = spreadArea() * SEED_EDGE_RATIO * (spreadArea() * SEED_EDGE_RATIO);
        for (BlockPos seed : TaintSeedRegistry.get(serverLevel).all()) {
            double d = seed.distSqr(pos);
            if (d < area && d > fringe) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTaintBlock(BlockState state) {
        return state.getBlock() instanceof ITaintBlock;
    }

    public static boolean hasSturdyNeighbour(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (facesSturdily(level, pos, direction)) {
                return true;
            }
        }
        return false;
    }

    public static boolean facesSturdily(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos neighbour = pos.relative(direction);
        return level.getBlockState(neighbour).isFaceSturdy(level, neighbour, direction.getOpposite());
    }

    public static boolean isRootless(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            BlockState neighbourState = level.getBlockState(neighbour);
            if (!(neighbourState.getBlock() instanceof ITaintBlock)
                    && neighbourState.isFaceSturdy(level, neighbour, direction.getOpposite())) {
                return false;
            }
        }
        return true;
    }

    public static int countAdjacentTaint(LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).getBlock() instanceof ITaintBlock) {
                count++;
            }
        }
        return count;
    }

    public static boolean placeFibreFromFlux(ServerLevel level, BlockPos pos) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || !ThaumaturgeCommonConfig.TAINT_FROM_FLUX.get()
                || TaintBlooms.isProtected(level, pos)) {
            return false;
        }
        return level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
    }

    public static void attemptFibreGrowth(ServerLevel level, BlockPos origin, boolean force) {
        FibreGrowth.attempt(level, origin, force);
    }

    public static boolean canHostFoothold(BlockState state) {
        return PhysicalFlux.isPhysicalFlux(state)
                || state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    public static void establishFoothold(ServerLevel level, BlockPos pos, float pressure, int spreadAttempts) {
        if (canHostFoothold(level.getBlockState(pos))) {
            level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
        }
        TaintEcology.addPressure(level, pos, pressure);
        for (int attempt = 0; attempt < spreadAttempts; attempt++) {
            attemptFibreGrowth(level, pos, true);
        }
    }

    public static boolean trySpreadTaintedBiome(ServerLevel level, BlockPos pos, RandomSource random) {
        return TaintFrontier.tryAdvance(level, pos, random);
    }

    public static boolean trySpawnSatelliteSeed(ServerLevel level, BlockPos origin, RandomSource random) {
        return TaintSprouting.trySatellite(level, origin, random);
    }
}
