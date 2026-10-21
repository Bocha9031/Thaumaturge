package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class TubeBufferRenderState extends BlockEntityRenderState {
    public final int[] bandColors = new int[Direction.values().length];
    public boolean hasBands;
}
