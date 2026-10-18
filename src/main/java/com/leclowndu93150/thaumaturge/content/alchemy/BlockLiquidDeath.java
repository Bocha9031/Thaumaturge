package com.leclowndu93150.thaumaturge.content.alchemy;

import com.leclowndu93150.thaumaturge.content.particle.SlimyBubbleParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class BlockLiquidDeath extends LiquidBlock {
    private static final int BUBBLE_ODDS = 6;
    private static final int POP_ODDS = 60;
    private static final int BUBBLE_COLOR = ARGB32.color(0x5A, 0x14, 0x78);
    private static final float BUBBLE_ALPHA = 0.75F;
    private static final float BUBBLE_MIN_SCALE = 0.12F;
    private static final float BUBBLE_SCALE_SPREAD = 0.1F;
    private static final int BUBBLE_MIN_AGE = 12;
    private static final int BUBBLE_AGE_SPREAD = 6;
    private static final double POP_HEIGHT = 0.6;
    private static final float POP_MIN_VOLUME = 0.1F;
    private static final float POP_VOLUME_SPREAD = 0.1F;
    private static final float POP_MIN_PITCH = 0.75F;
    private static final float POP_PITCH_SPREAD = 0.2F;

    public BlockLiquidDeath(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        LiquidDeathFluid.applyEntityInside(level, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(BUBBLE_ODDS) == 0) {
            double surface = pos.getY() + state.getFluidState().getHeight(level, pos);
            float scale = BUBBLE_MIN_SCALE + random.nextFloat() * BUBBLE_SCALE_SPREAD;
            int age = BUBBLE_MIN_AGE + random.nextInt(BUBBLE_AGE_SPREAD);
            SlimyBubbleParticleOptions bubble = new SlimyBubbleParticleOptions(BUBBLE_COLOR, BUBBLE_ALPHA, scale, age);
            level.addParticle(
                    bubble, pos.getX() + random.nextDouble(), surface, pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
        }
        if (random.nextInt(POP_ODDS) == 0) {
            float volume = POP_MIN_VOLUME + random.nextFloat() * POP_VOLUME_SPREAD;
            float pitch = POP_MIN_PITCH + random.nextFloat() * POP_PITCH_SPREAD;
            level.playLocalSound(
                    pos.getX() + random.nextDouble(),
                    pos.getY() + POP_HEIGHT,
                    pos.getZ() + random.nextDouble(),
                    SoundEvents.LAVA_POP,
                    SoundSource.BLOCKS,
                    volume,
                    pitch,
                    false);
        }
    }
}
