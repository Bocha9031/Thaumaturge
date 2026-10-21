package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public final class WorkbenchWandRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState wand = new ItemStackRenderState();
    public boolean hasWand;
    public double centerX;
    public double centerY;
    public double centerZ;
    public double length;
    public double thickness;
}
