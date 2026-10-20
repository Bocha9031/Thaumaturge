package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.workbench.BlockEntityArcaneWorkbench;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class WorkbenchWandDisplayRenderer implements BlockEntityRenderer<BlockEntityArcaneWorkbench> {
    private static final float TABLE_CENTER = 0.5F;
    private static final float SURFACE_HEIGHT = 1.0F + 1.0F / 16.0F;
    private static final float CROSSWISE_ANGLE = 45.0F;
    private static final float TABLETOP_SCALE = 1.2F;

    public WorkbenchWandDisplayRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityArcaneWorkbench workbench,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        ItemStack wand = workbench.getInventory().wandStack();
        if (wand.isEmpty() || !(wand.getItem() instanceof ItemWand)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(TABLE_CENTER, SURFACE_HEIGHT, TABLE_CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(CROSSWISE_ANGLE));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(TABLETOP_SCALE, TABLETOP_SCALE, TABLETOP_SCALE);
        ItemRenderHelper.render(
                wand,
                ItemDisplayContext.GROUND,
                poseStack,
                buffers,
                light,
                overlay,
                workbench.getBlockPos().hashCode());
        poseStack.popPose();
    }
}
