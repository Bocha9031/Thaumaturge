package com.leclowndu93150.thaumaturge.content.world.taint;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintacle;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

public final class TaintBiomeFeature extends Feature<TaintBiomeConfig> {
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS;
    private static final int CHUNK_WIDTH = 16;
    private static final int CHUNK_CENTER_QUART = 2;
    private static final float FULL_TURN = 360.0F;

    public TaintBiomeFeature(Codec<TaintBiomeConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<TaintBiomeConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        TaintBiomeConfig config = context.config();
        ChunkPos chunk = new ChunkPos(context.origin());
        boolean any = false;

        int blobs = random.nextInt(config.maxCrustBlobs() + 1);
        for (int i = 0; i < blobs; i++) {
            int x = chunk.getMinBlockX() + random.nextInt(CHUNK_WIDTH);
            int z = chunk.getMinBlockZ() + random.nextInt(CHUNK_WIDTH);
            any |= placeCrustBlob(level, random, config, x, z);
        }
        for (int i = 0; i < config.grassFibreAttempts(); i++) {
            int x = chunk.getMinBlockX() + random.nextInt(CHUNK_WIDTH);
            int z = chunk.getMinBlockZ() + random.nextInt(CHUNK_WIDTH);
            any |= placeFibre(level, config, x, z, true);
        }
        for (int i = 0; i < config.generalFibreAttempts(); i++) {
            int x = chunk.getMinBlockX() + random.nextInt(CHUNK_WIDTH);
            int z = chunk.getMinBlockZ() + random.nextInt(CHUNK_WIDTH);
            any |= placeFibre(level, config, x, z, false);
        }
        if (config.landmarks()
                && !ThaumaturgeCommonConfig.WUSS_MODE.get()
                && isLandmarkChunk(level, chunk, config.landmarkRadiusChunks())) {
            any |= placeNode(level, random, config, chunk, true);
            any |= placeNode(level, random, config, chunk, false);
            any |= placeTaintacle(level, random, config, chunk);
        }
        return any;
    }

    private static boolean placeFibre(WorldGenLevel level, TaintBiomeConfig config, int x, int z, boolean onDirt) {
        BlockPos ground = ground(level, x, z, config.groundSearchDepth());
        if (ground == null || onDirt && !level.getBlockState(ground).is(BlockTags.DIRT)) {
            return false;
        }
        BlockPos target = ground.above();
        if (!canHostFibre(level, target)) {
            return false;
        }
        level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), PLACE_FLAGS);
        return true;
    }

    private static boolean placeCrustBlob(
            WorldGenLevel level, RandomSource random, TaintBiomeConfig config, int x, int z) {
        BlockPos center = ground(level, x, z, config.groundSearchDepth());
        if (center == null || !level.getBiome(center).is(TTBiomeTags.IS_TAINTED)) {
            return false;
        }

        int radius = config.crustRadius().sample(random);
        boolean any = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + random.nextInt(2)) {
                    continue;
                }
                BlockPos pos = ground(level, x + dx, z + dz, config.groundSearchDepth());
                if (pos != null && level.getBlockState(pos).getDestroySpeed(level, pos) >= 0.0F) {
                    level.setBlock(pos, config.crust().defaultBlockState(), PLACE_FLAGS);
                    any = true;
                }
            }
        }
        return any;
    }

    private static boolean placeNode(
            WorldGenLevel level, RandomSource random, TaintBiomeConfig config, ChunkPos chunk, boolean tainted) {
        BlockPos surface = findTaintedSurface(level, random, config, chunk);
        if (surface == null) {
            return false;
        }
        return tainted
                ? NodeGenerator.createGuaranteedTaintedNodeAt(level, surface, random)
                : NodeGenerator.createGuaranteedHungryNodeAt(level, surface, random);
    }

    private static boolean placeTaintacle(
            WorldGenLevel level, RandomSource random, TaintBiomeConfig config, ChunkPos chunk) {
        if (level.getLevel().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        BlockPos pos = findTaintedSurface(level, random, config, chunk);
        if (pos == null) {
            return false;
        }

        EntityTaintacle taintacle = TTEntities.TAINTACLE.get().create(level.getLevel());
        if (taintacle == null) {
            return false;
        }
        level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), PLACE_FLAGS);
        taintacle.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * FULL_TURN, 0.0F);
        EventHooks.finalizeMobSpawn(
                taintacle, level, level.getCurrentDifficultyAt(pos), MobSpawnType.CHUNK_GENERATION, null);
        taintacle.setPersistenceRequired();
        level.addFreshEntityWithPassengers(taintacle);
        return true;
    }

    @Nullable
    private static BlockPos findTaintedSurface(
            WorldGenLevel level, RandomSource random, TaintBiomeConfig config, ChunkPos chunk) {
        for (int attempt = 0; attempt < config.landmarkAttempts(); attempt++) {
            int x = chunk.getMinBlockX() + random.nextInt(CHUNK_WIDTH);
            int z = chunk.getMinBlockZ() + random.nextInt(CHUNK_WIDTH);
            BlockPos ground = ground(level, x, z, config.groundSearchDepth());
            if (ground != null
                    && ground.getY() + 1 < level.getMaxBuildHeight()
                    && canHostFibre(level, ground.above())
                    && level.getBlockState(ground).getDestroySpeed(level, ground) >= 0.0F) {
                return ground.above();
            }
        }
        return null;
    }

    private static boolean canHostFibre(WorldGenLevel level, BlockPos pos) {
        BlockState here = level.getBlockState(pos);
        return level.getBiome(pos).is(TTBiomeTags.IS_TAINTED)
                && (here.isAir() || here.canBeReplaced())
                && here.getFluidState().isEmpty()
                && BlockTaintFibre.hasSolidAttachment(level, pos);
    }

    @Nullable
    private static BlockPos ground(WorldGenLevel level, int x, int z, int depth) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
        int floor = Math.max(level.getMinBuildHeight(), cursor.getY() - depth);
        while (cursor.getY() >= floor) {
            BlockState state = level.getBlockState(cursor);
            if (!state.getFluidState().isEmpty()) {
                return null;
            }
            if (!state.isAir() && !state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES) && !state.canBeReplaced()) {
                return cursor.immutable();
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    private static boolean isLandmarkChunk(WorldGenLevel level, ChunkPos chunk, int radius) {
        BiomeSource source = level.getLevel().getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler =
                level.getLevel().getChunkSource().randomState().sampler();
        int quartY = QuartPos.fromBlock(level.getSeaLevel());
        if (!isNaturallyTainted(source, sampler, chunk.x, chunk.z, quartY)) {
            return false;
        }

        long seed = level.getSeed();
        long own = mix(seed, chunk.x, chunk.z);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int otherX = chunk.x + dx;
                int otherZ = chunk.z + dz;
                if ((dx != 0 || dz != 0)
                        && Long.compareUnsigned(mix(seed, otherX, otherZ), own) < 0
                        && isNaturallyTainted(source, sampler, otherX, otherZ, quartY)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isNaturallyTainted(
            BiomeSource source, Climate.Sampler sampler, int chunkX, int chunkZ, int quartY) {
        int quartX = QuartPos.fromSection(chunkX) + CHUNK_CENTER_QUART;
        int quartZ = QuartPos.fromSection(chunkZ) + CHUNK_CENTER_QUART;
        return source.getNoiseBiome(quartX, quartY, quartZ, sampler).is(TTBiomeTags.IS_TAINTED);
    }

    private static long mix(long seed, int x, int z) {
        long value = seed ^ x * 341873128712L ^ z * 132897987541L;
        value ^= value >>> 30;
        value *= 0xbf58476d1ce4e5b9L;
        value ^= value >>> 27;
        value *= 0x94d049bb133111ebL;
        return value ^ value >>> 31;
    }
}
