package com.leclowndu93150.thaumaturge.content.world.tree;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public final class BlockWideTrunkLog extends RotatedPillarBlock {
    public static final MapCodec<BlockWideTrunkLog> CODEC = simpleCodec(BlockWideTrunkLog::new);
    private static final int REFRESH_RADIUS = 2;
    private static final int REFRESH_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    public BlockWideTrunkLog(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(TrunkQuadrant.PROPERTY, TrunkQuadrant.SINGLE));
    }

    @Override
    public MapCodec<BlockWideTrunkLog> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TrunkQuadrant.PROPERTY);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return resolveQuadrant(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        BlockState updated = super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
        if (directionToNeighbour.getAxis().isVertical()) {
            return updated;
        }
        return resolveQuadrant(updated, level, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(this) || oldState.getValue(AXIS) != state.getValue(AXIS)) {
            refreshNearby(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        refreshNearby(level, pos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState rotated = super.rotate(state, rotation);
        return rotated.setValue(TrunkQuadrant.PROPERTY, state.getValue(TrunkQuadrant.PROPERTY).rotated(rotation));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(TrunkQuadrant.PROPERTY, state.getValue(TrunkQuadrant.PROPERTY).mirrored(mirror));
    }

    private BlockState resolveQuadrant(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(AXIS) != Direction.Axis.Y) {
            return state.setValue(TrunkQuadrant.PROPERTY, TrunkQuadrant.SINGLE);
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        if (!horizontalNeighboursLoaded(level, pos, cursor)) {
            return state;
        }
        return state.setValue(TrunkQuadrant.PROPERTY, deriveQuadrant(level, pos, cursor));
    }

    private static boolean horizontalNeighboursLoaded(LevelReader level, BlockPos pos, BlockPos.MutableBlockPos cursor) {
        return level.hasChunkAt(cursor.setWithOffset(pos, REFRESH_RADIUS, 0, REFRESH_RADIUS)) && level.hasChunkAt(cursor.setWithOffset(pos, -REFRESH_RADIUS, 0, REFRESH_RADIUS))
                && level.hasChunkAt(cursor.setWithOffset(pos, REFRESH_RADIUS, 0, -REFRESH_RADIUS)) && level.hasChunkAt(cursor.setWithOffset(pos, -REFRESH_RADIUS, 0, -REFRESH_RADIUS));
    }

    private TrunkQuadrant deriveQuadrant(LevelReader level, BlockPos pos, BlockPos.MutableBlockPos cursor) {
        if (isSquare(level, pos, 1, 1, cursor)) {
            return TrunkQuadrant.NORTH_WEST;
        }
        if (isSquare(level, pos, -1, 1, cursor)) {
            return TrunkQuadrant.NORTH_EAST;
        }
        if (isSquare(level, pos, 1, -1, cursor)) {
            return TrunkQuadrant.SOUTH_WEST;
        }
        if (isSquare(level, pos, -1, -1, cursor)) {
            return TrunkQuadrant.SOUTH_EAST;
        }
        return TrunkQuadrant.SINGLE;
    }

    private boolean isSquare(LevelReader level, BlockPos pos, int dx, int dz, BlockPos.MutableBlockPos cursor) {
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                boolean self = x == 0 && z == 0;
                if (!self && !isTrunkNeighbour(level, cursor.setWithOffset(pos, x * dx, 0, z * dz))) {
                    return false;
                }
                int outX = x == 0 ? -dx : 2 * dx;
                int outZ = z == 0 ? -dz : 2 * dz;
                if (isTrunkNeighbour(level, cursor.setWithOffset(pos, outX, 0, z * dz)) || isTrunkNeighbour(level, cursor.setWithOffset(pos, x * dx, 0, outZ))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void refreshNearby(Level level, BlockPos changed) {
        if (level.isClientSide()) {
            return;
        }
        BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
        for (int dx = -REFRESH_RADIUS; dx <= REFRESH_RADIUS; dx++) {
            for (int dz = -REFRESH_RADIUS; dz <= REFRESH_RADIUS; dz++) {
                target.setWithOffset(changed, dx, 0, dz);
                if (!level.hasChunkAt(target)) {
                    continue;
                }
                BlockState state = level.getBlockState(target);
                if (state.is(this)) {
                    BlockState resolved = resolveQuadrant(state, level, target);
                    if (resolved != state) {
                        level.setBlock(target.immutable(), resolved, REFRESH_FLAGS);
                    }
                }
            }
        }
    }

    private boolean isTrunkNeighbour(LevelReader level, BlockPos neighbour) {
        BlockState neighbourState = level.getBlockState(neighbour);
        return neighbourState.is(this) && neighbourState.getValue(AXIS) == Direction.Axis.Y;
    }
}
