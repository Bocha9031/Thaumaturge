package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class ScatteredFlowersDecorator extends TreeDecorator {
    public static final MapCodec<ScatteredFlowersDecorator> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(BuiltInRegistries.BLOCK
                            .byNameCodec()
                            .fieldOf("flower")
                            .forGetter(decorator -> decorator.flower))
                    .apply(instance, ScatteredFlowersDecorator::new));

    private static final int FLOWER_MAX_COUNT = 18;
    private static final int FLOWER_BOX_HALF_WIDTH = 7;
    private static final int FLOWER_BOX_HALF_HEIGHT = 3;

    private final Block flower;

    public ScatteredFlowersDecorator(Block flower) {
        this.flower = flower;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TTTreePlacers.SCATTERED_FLOWERS.get();
    }

    @Override
    public void place(TreeDecorator.Context context) {
        ObjectArrayList<BlockPos> logs = context.logs();
        if (logs.isEmpty()) {
            return;
        }
        placeAround(
                findFoot(logs),
                context.random(),
                context::isAir,
                spot -> context.level().isStateAtPosition(spot.below(), ScatteredFlowersDecorator::isValidSoil),
                spot -> context.setBlock(spot, flower.defaultBlockState()));
    }

    public static void placeAround(
            BlockPos foot,
            RandomSource random,
            Predicate<BlockPos> isAir,
            Predicate<BlockPos> hasSoil,
            Consumer<BlockPos> placeFlower) {
        for (int attempt = 0; attempt < FLOWER_MAX_COUNT; attempt++) {
            BlockPos spot = foot.offset(
                    spread(random, FLOWER_BOX_HALF_WIDTH),
                    spread(random, FLOWER_BOX_HALF_HEIGHT),
                    spread(random, FLOWER_BOX_HALF_WIDTH));
            if (isAir.test(spot) && hasSoil.test(spot)) {
                placeFlower.accept(spot);
            }
        }
    }

    public static boolean isValidSoil(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.SAND);
    }

    private static int spread(RandomSource random, int halfSize) {
        return random.nextInt(halfSize + 1) - random.nextInt(halfSize + 1);
    }

    static BlockPos findFoot(ObjectArrayList<BlockPos> logs) {
        Long2IntOpenHashMap counts = new Long2IntOpenHashMap();
        for (BlockPos log : logs) {
            counts.addTo(BlockPos.asLong(log.getX(), 0, log.getZ()), 1);
        }
        BlockPos trunk = null;
        int best = 0;
        for (BlockPos log : logs) {
            int count = counts.get(BlockPos.asLong(log.getX(), 0, log.getZ()));
            if (count > best || (count == best && isBefore(log, trunk))) {
                best = count;
                trunk = log;
            }
        }
        BlockPos foot = trunk;
        for (BlockPos log : logs) {
            if (log.getX() == trunk.getX() && log.getZ() == trunk.getZ() && log.getY() < foot.getY()) {
                foot = log;
            }
        }
        return foot;
    }

    private static boolean isBefore(BlockPos candidate, BlockPos current) {
        return candidate.getX() < current.getX()
                || (candidate.getX() == current.getX() && candidate.getZ() < current.getZ());
    }
}
