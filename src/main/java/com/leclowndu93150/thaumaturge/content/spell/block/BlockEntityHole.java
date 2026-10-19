package com.leclowndu93150.thaumaturge.content.spell.block;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityHole extends AbstractSyncedBlockEntity {
    private static final int DEFAULT_COUNTDOWN_MAX = 120;
    private static final int DIRTY_INTERVAL_TICKS = 20;
    private static final int SPARKLE_ATTEMPTS_PER_TICK = 2;
    private static final int SPARKLE_SKIP_ODDS = 3;
    private static final int EDGES_PER_AXIS = 4;
    private static final int EDGE_COUNT = 12;
    private static final int AXIS_COUNT = 3;
    private static final int FIRST_SIDE_BIT = 1;
    private static final int SECOND_SIDE_BIT = 2;
    private static final int RING_CELLS = 9;
    private static final int RING_SIZE = 3;
    private static final float SPARKLE_RED = 0.25F;
    private static final float SPARKLE_GREEN = 0.25F;
    private static final float SPARKLE_BLUE = 1.0F;
    private static final float SPARKLE_ALPHA = 1.0F;
    private static final Direction.Axis[] AXES = Direction.Axis.values();

    BlockState oldblock = Blocks.AIR.defaultBlockState();
    int countdown = 0;
    int countdownmax = DEFAULT_COUNTDOWN_MAX;
    int count = 0;

    @Nullable
    Direction direction = null;

    public BlockEntityHole(BlockPos pos, BlockState state) {
        super(TTBlockEntities.HOLE.get(), pos, state);
    }

    public void configure(BlockState oldblock, int countdownmax, int count, @Nullable Direction direction) {
        this.oldblock = oldblock;
        this.countdownmax = countdownmax;
        this.count = count;
        this.direction = direction;
    }

    public BlockState originalBlockState() {
        return oldblock;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityHole hole) {
        if (hole.countdown == 0 && hole.count > 1 && hole.direction != null) {
            switch (hole.direction.getAxis()) {
                case Y -> {
                    for (int a = 0; a < RING_CELLS; a++) {
                        if (a / RING_SIZE != 1 || a % RING_SIZE != 1) {
                            PortableHoles.open(
                                    level,
                                    pos.offset(-1 + a / RING_SIZE, 0, -1 + a % RING_SIZE),
                                    null,
                                    1,
                                    hole.countdownmax);
                        }
                    }
                }
                case Z -> {
                    for (int a = 0; a < RING_CELLS; a++) {
                        if (a / RING_SIZE != 1 || a % RING_SIZE != 1) {
                            PortableHoles.open(
                                    level,
                                    pos.offset(-1 + a / RING_SIZE, -1 + a % RING_SIZE, 0),
                                    null,
                                    1,
                                    hole.countdownmax);
                        }
                    }
                }
                case X -> {
                    for (int a = 0; a < RING_CELLS; a++) {
                        if (a / RING_SIZE != 1 || a % RING_SIZE != 1) {
                            PortableHoles.open(
                                    level,
                                    pos.offset(0, -1 + a / RING_SIZE, -1 + a % RING_SIZE),
                                    null,
                                    1,
                                    hole.countdownmax);
                        }
                    }
                }
            }
            if (!PortableHoles.open(
                    level,
                    pos.relative(hole.direction.getOpposite()),
                    hole.direction,
                    hole.count - 1,
                    hole.countdownmax)) {
                hole.count = 0;
            }
        }
        hole.countdown++;
        if (hole.countdown % DIRTY_INTERVAL_TICKS == 0) {
            hole.setChanged();
        }
        if (hole.countdown >= hole.countdownmax) {
            level.setBlock(pos, hole.oldblock, Block.UPDATE_ALL);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityHole hole) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < SPARKLE_ATTEMPTS_PER_TICK; attempt++) {
            hole.sparkle(level, pos, random);
        }
    }

    private void sparkle(Level level, BlockPos pos, RandomSource random) {
        int edge = random.nextInt(EDGE_COUNT);
        Direction.Axis free = AXES[edge / EDGES_PER_AXIS];
        Direction.Axis first = AXES[(free.ordinal() + 1) % AXIS_COUNT];
        Direction.Axis second = AXES[(free.ordinal() + 2) % AXIS_COUNT];
        int firstSide = (edge & FIRST_SIDE_BIT) == 0 ? -1 : 1;
        int secondSide = (edge & SECOND_SIDE_BIT) == 0 ? -1 : 1;
        if (!exposed(level, pos, first, firstSide, second, secondSide) || random.nextInt(SPARKLE_SKIP_ODDS) == 0) {
            return;
        }
        double along = random.nextDouble();
        double x = pos.getX() + offset(Direction.Axis.X, free, along, first, firstSide, secondSide);
        double y = pos.getY() + offset(Direction.Axis.Y, free, along, first, firstSide, secondSide);
        double z = pos.getZ() + offset(Direction.Axis.Z, free, along, first, firstSide, secondSide);
        SparkleParticleOptions data = new SparkleParticleOptions(
                ARGB32.color(
                        (int) (SPARKLE_ALPHA * 255.0F),
                        (int) (SPARKLE_RED * 255.0F),
                        (int) (SPARKLE_GREEN * 255.0F),
                        (int) (SPARKLE_BLUE * 255.0F)),
                0.6F + random.nextFloat() * 0.2F,
                0,
                1.0F,
                0.0F,
                2,
                false);
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }

    private static double offset(
            Direction.Axis axis,
            Direction.Axis free,
            double along,
            Direction.Axis first,
            int firstSide,
            int secondSide) {
        if (axis == free) {
            return along;
        }
        return (axis == first ? firstSide : secondSide) > 0 ? 1.0 : 0.0;
    }

    private static boolean exposed(
            Level level, BlockPos pos, Direction.Axis first, int firstSide, Direction.Axis second, int secondSide) {
        BlockPos firstNeighbour = pos.relative(first, firstSide);
        BlockPos secondNeighbour = pos.relative(second, secondSide);
        BlockPos diagonal = firstNeighbour.relative(second, secondSide);
        return isExposed(
                open(level, firstNeighbour),
                open(level, secondNeighbour),
                opaque(level, firstNeighbour),
                opaque(level, secondNeighbour),
                opaque(level, diagonal));
    }

    static boolean isExposed(
            boolean firstOpen, boolean secondOpen, boolean firstOpaque, boolean secondOpaque, boolean diagonalOpaque) {
        return firstOpen && (secondOpaque || diagonalOpaque) || secondOpen && (firstOpaque || diagonalOpaque);
    }

    private static boolean open(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.is(TTBlocks.HOLE.get()) && !state.isSolidRender(level, pos);
    }

    private static boolean opaque(Level level, BlockPos pos) {
        return level.getBlockState(pos).isSolidRender(level, pos);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TTNbt.store(output, "oldblock", BlockState.CODEC, registries, oldblock);
        output.putInt("countdown", countdown);
        output.putInt("countdownmax", countdownmax);
        output.putInt("count", count);
        output.putInt("direction", direction == null ? -1 : direction.ordinal());
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        oldblock = TTNbt.read(input, "oldblock", BlockState.CODEC, registries).orElse(Blocks.AIR.defaultBlockState());
        countdown = (input.contains("countdown") ? input.getInt("countdown") : 0);
        countdownmax = (input.contains("countdownmax") ? input.getInt("countdownmax") : DEFAULT_COUNTDOWN_MAX);
        count = (input.contains("count") ? input.getInt("count") : 0);
        int directionOrdinal = (input.contains("direction") ? input.getInt("direction") : -1);
        direction = directionOrdinal >= 0 ? Direction.values()[directionOrdinal] : null;
    }
}
