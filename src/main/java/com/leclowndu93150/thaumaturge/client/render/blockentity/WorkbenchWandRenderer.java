package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.workbench.BlockEntityArcaneWorkbench;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class WorkbenchWandRenderer implements BlockEntityRenderer<BlockEntityArcaneWorkbench, WorkbenchWandRenderState> {
    private static final float TABLE_CENTER = 0.5F;
    private static final float SURFACE_HEIGHT = 1.0F;
    private static final float GRID_SIZE_PIXELS = 10.0F;
    private static final float PIXELS_PER_BLOCK = 16.0F;
    private static final float GRID_DIAGONAL = (float) (GRID_SIZE_PIXELS * Math.sqrt(2.0) / PIXELS_PER_BLOCK);
    private static final float DIAGONAL_DEGREES = 45.0F;
    private static final float LAY_FLAT_DEGREES = 90.0F;
    private static final int OUTLINE_NONE = 0;

    private final ItemModelResolver itemModelResolver;

    public WorkbenchWandRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public WorkbenchWandRenderState createRenderState() {
        return new WorkbenchWandRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityArcaneWorkbench workbench, WorkbenchWandRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(workbench, state, partialTicks, cameraPosition, breakProgress);
        ItemStack wand = workbench.getInventory().wandStack();
        state.hasWand = !wand.isEmpty() && workbench.getLevel() != null;
        if (!state.hasWand) {
            return;
        }
        itemModelResolver.updateForTopItem(state.wand, wand, ItemDisplayContext.NONE, workbench.getLevel(), null, (int) workbench.getBlockPos().asLong());
        AABB bounds = state.wand.getModelBoundingBox();
        state.hasWand = bounds.getYsize() > 0.0;
        state.centerX = bounds.getCenter().x;
        state.centerY = bounds.getCenter().y;
        state.centerZ = bounds.getCenter().z;
        state.length = bounds.getYsize();
        state.thickness = bounds.getZsize();
    }

    @Override
    public void submit(WorkbenchWandRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.hasWand) {
            return;
        }
        float scale = (float) (GRID_DIAGONAL / state.length);
        poseStack.pushPose();
        poseStack.translate(TABLE_CENTER, SURFACE_HEIGHT + state.thickness * scale / 2.0, TABLE_CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(DIAGONAL_DEGREES));
        poseStack.mulPose(Axis.XP.rotationDegrees(LAY_FLAT_DEGREES));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-state.centerX, -state.centerY, -state.centerZ);
        state.wand.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, OUTLINE_NONE);
        poseStack.popPose();
    }
}
