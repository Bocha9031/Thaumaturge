package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.client.golems.GolemPartRenderView;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryStateView;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.accessory.GolemAccessoryStates;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class GolemRenderState implements GolemPartRenderView {
    public GolemProperties props;
    public byte color;
    public float ageInTicks;
    public int lightCoords;
    public int outlineColor;
    public float bodyRot;
    public float headYawDelta;
    public float pitch;
    public float walkPos;
    public float walkSpeed;
    public float attackTime;
    public double speedSq;
    public float yawDelta;
    public float wheelRotation;
    public boolean combat;
    public boolean invisible;
    public boolean ghost;
    public boolean xray;
    public ItemStack heldItem = ItemStack.EMPTY;
    public boolean holdingItem;
    public ItemStack haulerItem = ItemStack.EMPTY;
    public boolean haulingItem;
    public boolean heldItemIsBlock;
    public boolean haulerItemIsBlock;
    public List<GolemAccessory> accessories = List.of();
    public GolemAccessoryStateView accessoryStates = GolemAccessoryStates.EMPTY;

    @Override
    public float swingProgress() {
        return attackTime;
    }

    @Override
    public float ageInTicks() {
        return ageInTicks;
    }

    @Override
    public float headPitch() {
        return pitch;
    }

    @Override
    public boolean fighting() {
        return combat;
    }

    @Override
    public float wheelRotation() {
        return wheelRotation;
    }

    @Override
    public int lightCoords() {
        return lightCoords;
    }

    @Override
    public int outlineColor() {
        return outlineColor;
    }

    @Override
    public boolean hasSecondHandItem() {
        return haulingItem;
    }

    @Override
    public boolean secondHandItemIsBlock() {
        return haulerItemIsBlock;
    }

    @Override
    public ItemStack secondHandItem() {
        return haulerItem;
    }
}
