package com.leclowndu93150.thaumaturge.mixin.world.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
    @Invoker("travelInAir")
    void thaumaturge$travelInAir(Vec3 input);
}
