package com.leclowndu93150.thaumaturge.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererInvoker {
    @Invoker("renderPlayerArm")
    void thaumaturge$renderPlayerArm(PoseStack poseStack, SubmitNodeCollector collector, int light, float inverseArmHeight, float attackValue, HumanoidArm arm);
}
