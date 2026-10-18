package com.leclowndu93150.thaumaturge.content.world.crystal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class BlockCrystal extends Block {
    private static final int MAX_SIZE = 3;
    private static final int MAX_GENERATION = 4;

    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, MAX_SIZE);
    public static final IntegerProperty GENERATION = IntegerProperty.create("gen", 1, MAX_GENERATION);

    private static final int TICK_CHANCE_BASE = 3;
    private static final int SPREAD_ONE_IN = 16;
    private static final int KEEP_GENERATION_ONE_IN = 6;
    private static final int SPREAD_REACH = 1;
    private static final int SPREAD_SPAN = SPREAD_REACH * 2 + 1;
    private static final int CEILING_BASE = 5;
    private static final long CEILING_VARIANTS = 3L;
    private static final float AURA_BAND = 10.0F;
    private static final float AURA_EXCHANGE = 10.0F;
    private static final float DECAY_FLUX = 1.0F;
    private static final float FLUX_CONVERSION_DIVISOR = 2.0F;
    private static final float DRAIN_TOLERANCE = 0.001F;
    private static final double[][][] SHARD_BOXES = {
        {{6.0, 0.0, 5.5, 10.0, 7.5, 9.0}, {7.0, 0.0, 9.0, 9.5, 6.0, 10.0}, {7.0, 6.5, 7.0, 9.0, 8.0, 8.5}},
        {{12.0, 0.0, 5.5, 15.0, 4.5, 7.5}, {13.0, 0.0, 7.5, 14.0, 3.5, 8.0}, {13.0, 2.0, 5.0, 13.5, 3.0, 5.5}},
        {{2.0, 0.0, 10.5, 4.5, 3.5, 12.0}, {2.0, 0.0, 12.0, 4.5, 3.0, 12.5}, {2.5, 3.0, 11.0, 4.0, 4.5, 12.5}},
        {{9.0, 0.0, 1.5, 12.0, 6.5, 3.5}, {10.0, 0.0, 3.5, 11.5, 5.0, 4.5}, {10.0, 2.5, 1.0, 10.5, 5.0, 1.5}},
        {{4.5, 0.0, 2.0, 7.5, 4.5, 3.5}, {5.0, 0.0, 1.0, 7.0, 4.0, 2.0}, {5.0, 0.0, 3.0, 7.0, 4.0, 4.0}},
        {{11.5, 0.0, 9.5, 14.0, 5.0, 12.5}, {12.5, 5.0, 10.5, 13.5, 6.0, 11.5}, {14.0, 2.5, 10.0, 14.5, 4.0, 10.5}},
        {{6.5, 0.0, 12.0, 9.5, 4.0, 14.5}, {7.0, 3.5, 12.5, 9.0, 5.5, 14.5}, {7.5, 1.5, 14.5, 9.0, 4.5, 15.0}},
        {{1.0, 0.0, 5.0, 4.0, 6.0, 7.0}, {1.5, 1.5, 4.5, 3.0, 4.0, 5.0}, {2.0, 0.5, 7.0, 3.5, 4.0, 7.5}}
    };

    private static final double SHAPE_SNAP = 32.0;

    private static final Map<Direction, List<VoxelShape>> SHARD_SHAPES = shardShapes();
    private static final int SHAPE_CACHE_LIMIT = 4096;

    private final ResourceKey<IAspect> aspect;
    private final boolean flux;
    private final Map<Long, VoxelShape> shapeCache = new ConcurrentHashMap<>();

    public BlockCrystal(BlockBehaviour.Properties properties, ResourceKey<IAspect> aspect, boolean flux) {
        super(properties);
        this.aspect = aspect;
        this.flux = flux;
        registerDefaultState(stateDefinition.any().setValue(SIZE, 0).setValue(GENERATION, 1));
    }

    private static final MapCodec<BlockCrystal> CODEC = simpleCodec(p -> new BlockCrystal(p, null, false));

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public ResourceKey<IAspect> aspect() {
        return aspect;
    }

    public boolean isFlux() {
        return flux;
    }

    public int growth(BlockState state) {
        return state.getValue(SIZE);
    }

    public int generation(BlockState state) {
        return state.getValue(GENERATION);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE, GENERATION);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        long seed = CrystalShards.seed(state, pos);
        int count = growth(state) + 1;
        long key = 0L;
        for (Direction face : Direction.values()) {
            if (!CrystalShards.supports(level, pos, face)) {
                continue;
            }
            List<Integer> order = CrystalShards.order(face, seed);
            long faceMask = 0L;
            for (int i = 0; i < count; i++) {
                faceMask |= 1L << order.get(i);
            }
            key |= faceMask << (face.ordinal() * CrystalShards.COUNT);
        }
        if (key == 0L) {
            return SHARD_SHAPES.get(Direction.DOWN).get(CrystalShards.unsupported(seed));
        }
        VoxelShape cached = shapeCache.get(key);
        if (cached != null) {
            return cached;
        }
        VoxelShape shape = buildShape(key);
        if (shapeCache.size() < SHAPE_CACHE_LIMIT) {
            shapeCache.put(key, shape);
        }
        return shape;
    }

    private static VoxelShape buildShape(long key) {
        List<VoxelShape> parts = new ArrayList<>();
        for (Direction face : Direction.values()) {
            List<VoxelShape> shards = SHARD_SHAPES.get(face);
            long faceMask = key >>> (face.ordinal() * CrystalShards.COUNT);
            for (int shard = 0; shard < CrystalShards.COUNT; shard++) {
                if ((faceMask & (1L << shard)) != 0L) {
                    parts.add(shards.get(shard));
                }
            }
        }
        return Shapes.or(Shapes.empty(), parts.toArray(new VoxelShape[0])).optimize();
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    private static Map<Direction, List<VoxelShape>> shardShapes() {
        Map<Direction, List<VoxelShape>> shapes = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            Matrix4f transform = CrystalFaceTransforms.forFace(face);
            List<VoxelShape> shards = new ArrayList<>();
            for (double[][] boxes : SHARD_BOXES) {
                VoxelShape shard = Shapes.empty();
                for (double[] box : boxes) {
                    shard = Shapes.or(shard, transformed(transform, box));
                }
                shards.add(shard.optimize());
            }
            shapes.put(face, List.copyOf(shards));
        }
        return shapes;
    }

    private static VoxelShape transformed(Matrix4f transform, double[] box) {
        Vector3f from = transform.transformPosition(
                new Vector3f((float) (box[0] / 16.0), (float) (box[1] / 16.0), (float) (box[2] / 16.0)));
        Vector3f to = transform.transformPosition(
                new Vector3f((float) (box[3] / 16.0), (float) (box[4] / 16.0), (float) (box[5] / 16.0)));
        return Shapes.box(
                snap(Math.min(from.x, to.x)),
                snap(Math.min(from.y, to.y)),
                snap(Math.min(from.z, to.z)),
                snap(Math.max(from.x, to.x)),
                snap(Math.max(from.y, to.y)),
                snap(Math.max(from.z, to.z)));
    }

    private static double snap(float value) {
        return Math.round(value * SHAPE_SNAP) / SHAPE_SNAP;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasSturdyNeighbour(level, pos);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbourState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighbourPos) {
        if (!state.canSurvive(level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(TICK_CHANCE_BASE + generation(state)) != 0) {
            return;
        }
        float stock = currentStock(level, pos);
        float base = AuraHelper.getAuraBase(level, pos);
        switch (bandFor(stock, base)) {
            case STARVED -> wither(state, level, pos);
            case RICH -> flourish(state, level, pos, random);
            case MIDDLE -> {
                if (!flux) {
                    convertToFlux(state, level, pos, stock, base);
                }
            }
        }
    }

    private static AuraBand bandFor(float stock, float base) {
        if (stock <= AURA_BAND) {
            return AuraBand.STARVED;
        }
        return stock > base + AURA_BAND ? AuraBand.RICH : AuraBand.MIDDLE;
    }

    private void wither(BlockState state, ServerLevel level, BlockPos pos) {
        int size = growth(state);
        if (size > 0) {
            level.setBlock(pos, state.setValue(SIZE, size - 1), 3);
            returnToAura(level, pos);
            return;
        }
        if (!hasSameCrystalBeside(level, pos)) {
            return;
        }
        level.removeBlock(pos, false);
        returnToAura(level, pos);
        if (!flux) {
            AuraHelper.addFlux(level, pos, DECAY_FLUX);
        }
    }

    private void flourish(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int size = growth(state);
        if (size < sizeCeiling(state, pos)) {
            if (drain(level, pos, AURA_EXCHANGE) > 0.0F) {
                level.setBlock(pos, state.setValue(SIZE, size + 1), 3);
            }
            return;
        }
        if (generation(state) < MAX_GENERATION) {
            seed(state, level, pos, random);
        }
    }

    static int sizeCeiling(BlockState state, BlockPos pos) {
        int growthBonus = (int) Math.floorMod(pos.asLong(), CEILING_VARIANTS);
        return Math.min(MAX_SIZE, CEILING_BASE - state.getValue(GENERATION) + growthBonus);
    }

    private void seed(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos target = neighbourWithin(pos, random);
        if (target.equals(pos)
                || !isOpenCell(level, target)
                || random.nextInt(SPREAD_ONE_IN) != 0
                || !hasSturdyNeighbour(level, target)) {
            return;
        }
        if (drain(level, pos, AURA_EXCHANGE) <= 0.0F) {
            return;
        }
        int parentGeneration = generation(state);
        int childGeneration = childGeneration(parentGeneration, random);
        level.setBlock(target, defaultBlockState().setValue(SIZE, 0).setValue(GENERATION, childGeneration), 3);
    }

    static BlockPos neighbourWithin(BlockPos pos, RandomSource random) {
        int dx = random.nextInt(SPREAD_SPAN) - SPREAD_REACH;
        int dy = random.nextInt(SPREAD_SPAN) - SPREAD_REACH;
        int dz = random.nextInt(SPREAD_SPAN) - SPREAD_REACH;
        return pos.offset(dx, dy, dz);
    }

    static int childGeneration(int parentGeneration, RandomSource random) {
        return random.nextInt(KEEP_GENERATION_ONE_IN) == 0
                ? parentGeneration
                : Math.min(parentGeneration + 1, MAX_GENERATION);
    }

    private static boolean isOpenCell(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty() && (state.isAir() || state.canBeReplaced());
    }

    private boolean hasSameCrystalBeside(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(this)) {
                return true;
            }
        }
        return false;
    }

    private float currentStock(ServerLevel level, BlockPos pos) {
        return flux ? AuraHelper.getFlux(level, pos) : AuraHelper.getVis(level, pos);
    }

    private void convertToFlux(BlockState state, ServerLevel level, BlockPos pos, float vis, float base) {
        float fluxStock = AuraHelper.getFlux(level, pos);
        if (fluxStock <= vis || fluxStock <= base / FLUX_CONVERSION_DIVISOR) {
            return;
        }
        float cost = growth(state) + 1;
        if (AuraHelper.drainFlux(level, pos, cost, false) < cost - DRAIN_TOLERANCE) {
            return;
        }
        BlockState tainted = TTBlocks.CRYSTAL_VITIUM
                .get()
                .defaultBlockState()
                .setValue(SIZE, growth(state))
                .setValue(GENERATION, generation(state));
        level.setBlock(pos, tainted, 3);
    }

    private float drain(ServerLevel level, BlockPos pos, float amount) {
        return flux ? AuraHelper.drainFlux(level, pos, amount, false) : AuraHelper.drainVis(level, pos, amount, false);
    }

    private void returnToAura(ServerLevel level, BlockPos pos) {
        if (flux) {
            AuraHelper.addFlux(level, pos, AURA_EXCHANGE);
        } else {
            AuraHelper.addVis(level, pos, AURA_EXCHANGE);
        }
    }

    private static boolean hasSturdyNeighbour(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            if (level.getBlockState(neighbour).isFaceSturdy(level, neighbour, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    private enum AuraBand {
        STARVED,
        RICH,
        MIDDLE
    }
}
