package com.leclowndu93150.thaumaturge.client.screen.pip;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public class PreviewBlockGetter implements BlockAndTintGetter {

    private final Map<BlockPos, BlockState> filledPositions;

    public PreviewBlockGetter(Map<BlockPos, BlockState> filledPositions) {
        this.filledPositions = new HashMap<>(filledPositions);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (filledPositions.containsKey(pos)) {
            return filledPositions.get(pos);
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return 256;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        return 0x7CBD6B;
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        return LightEngine.MAX_LEVEL;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int darkening) {
        return Math.max(0, LightEngine.MAX_LEVEL - darkening);
    }
}
