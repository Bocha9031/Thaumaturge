package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.content.fluid.WadingFluidType;
import net.minecraft.world.entity.Entity;

public final class FluxGooFluidType extends WadingFluidType {
    private static final double DRAG = 0.0001D;

    public FluxGooFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public double motionScale(Entity entity) {
        return DRAG;
    }
}
