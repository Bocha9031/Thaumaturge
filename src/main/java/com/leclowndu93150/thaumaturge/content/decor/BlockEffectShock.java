package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockEffectShock extends Block {
    public static final MapCodec<BlockEffectShock> CODEC = simpleCodec(BlockEffectShock::new);

    private static final float SHOCK_DAMAGE = 1.0F;
    private static final int SLOWNESS_DURATION = 20;
    private static final int SELF_REMOVE_ONE_IN = 100;
    private static final int SPARK_OPAQUE = 1;
    private static final double SPARK_MIN_HEIGHT = 1.0 / 7.0;
    private static final double SPARK_HEIGHT_SPREAD = 1.0 / 3.0 - SPARK_MIN_HEIGHT;
    private static final float SPARK_BASE_SCALE = 0.35F;
    private static final float SPARK_SCALE_PER_HEIGHT = 1.2F;
    private static final float SPARK_ALPHA = 0.85F;
    private static final float SPARK_RED_BASE = 0.7F;
    private static final float SPARK_RED_SPREAD = 0.2F;
    private static final float SPARK_GREEN_BASE = 0.9F;
    private static final float SPARK_GREEN_SPREAD = 0.1F;
    private static final float SPARK_BLUE = 1.0F;
    private static final int HUM_ODDS = 50;
    private static final float HUM_VOLUME = 0.1F;
    private static final float HUM_PITCH_BASE = 0.9F;
    private static final float HUM_PITCH_SPREAD = 0.2F;
    private static final double BLOCK_CENTER = 0.5;

    public BlockEffectShock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEffectShock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) {
            return;
        }
        if (entity instanceof LivingEntity living && level instanceof ServerLevel serverLevel) {
            living.hurt(serverLevel.damageSources().magic(), SHOCK_DAMAGE);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, 0, true, true));
        }
        if (level.getRandom().nextInt(SELF_REMOVE_ONE_IN) == 0) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double lift = SPARK_MIN_HEIGHT + random.nextDouble() * SPARK_HEIGHT_SPREAD;
        float scale = SPARK_BASE_SCALE + (float) lift * SPARK_SCALE_PER_HEIGHT;
        float red = SPARK_RED_BASE + random.nextFloat() * SPARK_RED_SPREAD;
        float green = SPARK_GREEN_BASE + random.nextFloat() * SPARK_GREEN_SPREAD;
        int color = ARGB32.colorFromFloat(SPARK_OPAQUE, red, green, SPARK_BLUE);
        level.addParticle(
                new SparkParticleOptions(color, SPARK_ALPHA, scale),
                pos.getX() + random.nextDouble(),
                pos.getY() + lift,
                pos.getZ() + random.nextDouble(),
                0.0,
                0.0,
                0.0);
        if (random.nextInt(HUM_ODDS) == 0) {
            float pitch = HUM_PITCH_BASE + random.nextFloat() * HUM_PITCH_SPREAD;
            level.playLocalSound(
                    pos.getX() + BLOCK_CENTER,
                    pos.getY() + BLOCK_CENTER,
                    pos.getZ() + BLOCK_CENTER,
                    TTSounds.JACOBS.get(),
                    SoundSource.AMBIENT,
                    HUM_VOLUME,
                    pitch,
                    false);
        }
    }
}
