package com.leclowndu93150.thaumaturge.content.fluid;

import com.leclowndu93150.thaumaturge.mixin.world.entity.LivingEntityInvoker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class WadingFluidType extends FluidType {
    protected WadingFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public boolean move(LivingEntity entity, Vec3 movementVector, double gravity) {
        ((LivingEntityInvoker) entity).thaumaturge$travelInAir(movementVector);
        return true;
    }
}
