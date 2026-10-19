package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.ChargeDisplay;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.entity.projectile.EntityGrapple;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class GrappleGunItem extends Item {
    private static final int MAX_CHARGE = 100;
    private static final float LAUNCH_PITCH_OFFSET = -3.0F;
    private static final float LAUNCH_VELOCITY = 1.5F;
    private static final float FIRE_VOLUME = 2.4F;
    private static final float FIRE_PITCH_BASE = 0.7F;
    private static final float FIRE_PITCH_SPREAD = 0.15F;
    private static final float RIGHT_YAW_OFFSET = 90.0F;
    private static final double FORWARD_REACH = 0.9;
    private static final double SIDE_SHIFT = 0.4;

    public GrappleGunItem(Properties properties) {
        super(properties.component(
                TTDataComponents.RECHARGEABLE.get(), new ChargeProfile(MAX_CHARGE, ChargeDisplay.ALWAYS)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !Boolean.TRUE.equals(stack.get(TTDataComponents.GRAPPLE_LOADED))) {
            return;
        }
        int tracked = entity.getData(TTAttachments.GRAPPLE_ID.get());
        if (tracked < 0 || !(level.getEntity(tracked) instanceof EntityGrapple grapple) || !grapple.isAlive()) {
            stack.remove(TTDataComponents.GRAPPLE_LOADED);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            float pitch = FIRE_PITCH_BASE + serverLevel.getRandom().nextFloat() * FIRE_PITCH_SPREAD;
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    TTSounds.ICE.get(),
                    SoundSource.PLAYERS,
                    FIRE_VOLUME,
                    pitch);
        }
        if (!level.isClientSide() && RechargeAccess.storedVis(stack) >= 1) {
            EntityGrapple grapple = new EntityGrapple(TTEntities.GRAPPLE.get(), level, player, hand);
            Vec3 spawn = spawnPoint(player, hand);
            grapple.setPos(spawn.x, grapple.getY(), spawn.z);
            grapple.shootFromRotation(
                    player, player.getXRot(), player.getYRot(), LAUNCH_PITCH_OFFSET, LAUNCH_VELOCITY, 0.0F);
            if (level.addFreshEntity(grapple)) {
                RechargeAccess.spendVis(stack, 1);
                stack.set(TTDataComponents.GRAPPLE_LOADED, true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    static Vec3 spawnPoint(Player player, InteractionHand hand) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        float sideYaw = player.getYRot() + (arm == HumanoidArm.RIGHT ? RIGHT_YAW_OFFSET : -RIGHT_YAW_OFFSET);
        Vec3 forward = horizontalHeading(player.getYRot()).scale(FORWARD_REACH);
        Vec3 side = horizontalHeading(sideYaw).scale(SIDE_SHIFT);
        return player.position().add(forward).add(side);
    }

    private static Vec3 horizontalHeading(float yawDegrees) {
        float radians = yawDegrees * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(radians), 0.0, Mth.cos(radians));
    }
}
