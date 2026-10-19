package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class ElementalHoeItem extends HoeItem {
    private static final int BONEMEAL_DAMAGE = 3;
    private static final int LEVEL_EVENT_BONEMEAL = 1505;
    private static final int BONEMEAL_EVENT_DATA = 15;
    private static final float PUFF_HEIGHT = 1.05F;

    public ElementalHoeItem(Properties properties) {
        super(TTMaterials.TOOL_ELEMENTAL, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || usesVanillaHoe(player)) {
            return super.useOn(context);
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean did = false;
        ItemStack hoe = context.getItemInHand();
        for (int xx = -1; xx <= 1 && !hoe.isEmpty(); xx++) {
            for (int zz = -1; zz <= 1 && !hoe.isEmpty(); zz++) {
                BlockPos pp = pos.offset(xx, 0, zz);
                UseOnContext offset = new UseOnContext(
                        player,
                        context.getHand(),
                        new BlockHitResult(Vec3.atCenterOf(pp), context.getClickedFace(), pp, false));
                if (super.useOn(offset).consumesAction()) {
                    if (level instanceof ServerLevel serverLevel) {
                        Effects.Bamf bamf = Effects.bamf(
                                        serverLevel,
                                        new Vec3(pp.getX() + 0.5, pp.getY() + PUFF_HEIGHT, pp.getZ() + 0.5))
                                .color(0.56F, 0.33F, 0.19F)
                                .side(Direction.UP);
                        if (xx == 0 && zz == 0) {
                            bamf.withSound();
                        }
                        bamf.send();
                    }
                    did = true;
                }
            }
        }
        if (did) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, player)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            context.getItemInHand()
                    .hurtAndBreak(BONEMEAL_DAMAGE, player, LivingEntity.getSlotForHand(context.getHand()));
            level.levelEvent(LEVEL_EVENT_BONEMEAL, pos, BONEMEAL_EVENT_DATA);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    static boolean usesVanillaHoe(Player player) {
        return player.isSecondaryUseActive();
    }
}
