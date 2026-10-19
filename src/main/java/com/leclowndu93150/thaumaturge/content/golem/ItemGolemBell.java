package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.golem.logistics.LogisticsGuiOpener;
import com.leclowndu93150.thaumaturge.content.golem.logistics.LogisticsTarget;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ItemGolemBell extends Item implements ISealDisplayer {
    private static final double AIM_LENGTH = 25.0D;
    private static final double AIM_STEP = 0.5D;
    static final int AIM_SAMPLES = (int) Math.round(AIM_LENGTH / AIM_STEP);
    private static final float CHIME_VOLUME = 0.5F;
    private static final float CHIME_PITCH_SPREAD = 0.2F;
    private static final float ZAP_VOLUME = 0.4F;

    public ItemGolemBell(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.swing(hand, true);
        if (level.isClientSide()) {
            player.playSound(
                    SoundEvents.NOTE_BLOCK_BELL.value(),
                    CHIME_VOLUME,
                    1.0F + level.getRandom().nextFloat() * CHIME_PITCH_SPREAD);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        ISealEntity seal = getAimedSeal(player);
        if (seal != null) {
            useOnSeal((ServerLevel) level, player, seal);
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        if (LogisticsGuiOpener.canOpen(player)) {
            LogisticsGuiOpener.open(player, null);
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        ISealEntity seal =
                SealHandler.getSealEntity(level, new SealPos(context.getClickedPos(), context.getClickedFace()));
        if (seal == null) {
            if (!LogisticsGuiOpener.canOpen(player)) {
                return InteractionResult.PASS;
            }
            player.swing(context.getHand(), true);
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            LogisticsGuiOpener.open(player, new LogisticsTarget(context.getClickedPos(), context.getClickedFace()));
            return InteractionResult.CONSUME;
        }
        player.swing(context.getHand(), true);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        useOnSeal((ServerLevel) level, player, seal);
        return InteractionResult.CONSUME;
    }

    private static void useOnSeal(ServerLevel level, Player player, ISealEntity seal) {
        if (!player.isShiftKeyDown()) {
            SealGuiOpener.open(player, seal);
        } else if (SealAccess.mayEdit(player, seal)) {
            SealHandler.removeSealEntity(level, seal.pos(), false);
            level.playSound(null, seal.pos().pos(), TTSounds.ZAP.get(), SoundSource.BLOCKS, ZAP_VOLUME, 1.0F);
        }
    }

    public static @Nullable ISealEntity getAimedSeal(Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        BlockPos previous = BlockPos.containing(start);
        for (int i = 1; i <= AIM_SAMPLES; i++) {
            BlockPos cell = BlockPos.containing(aimPoint(start, look, i));
            if (!cell.equals(previous)) {
                BlockPos delta = cell.subtract(previous);
                ISealEntity seal = sealOnEntryFace(player, cell, delta);
                if (seal != null) {
                    return seal;
                }
                if (!player.level()
                        .getBlockState(cell)
                        .getCollisionShape(player.level(), cell)
                        .isEmpty()) {
                    return null;
                }
                previous = cell;
            }
        }
        return null;
    }

    static Vec3 aimPoint(Vec3 origin, Vec3 look, int sample) {
        return origin.add(look.scale(sample * AIM_STEP));
    }

    private static @Nullable ISealEntity sealOnEntryFace(Player player, BlockPos cell, BlockPos delta) {
        if (delta.getX() != 0) {
            ISealEntity seal = SealHandler.getSealEntity(
                    player.level(), new SealPos(cell, delta.getX() > 0 ? Direction.WEST : Direction.EAST));
            if (seal != null) {
                return seal;
            }
        }
        if (delta.getY() != 0) {
            ISealEntity seal = SealHandler.getSealEntity(
                    player.level(), new SealPos(cell, delta.getY() > 0 ? Direction.DOWN : Direction.UP));
            if (seal != null) {
                return seal;
            }
        }
        if (delta.getZ() != 0) {
            return SealHandler.getSealEntity(
                    player.level(), new SealPos(cell, delta.getZ() > 0 ? Direction.NORTH : Direction.SOUTH));
        }
        return null;
    }
}
