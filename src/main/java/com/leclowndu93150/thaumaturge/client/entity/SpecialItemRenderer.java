package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;

public final class SpecialItemRenderer extends ItemEntityRenderer {
    private static final int FANCY_RAYS = 10;
    private static final int FAST_RAYS = 5;
    private static final float REACH = 0.95F;
    private static final float GROW_TICKS = 10.0F;
    private static final float SPIN_TICKS_PER_TURN = 500.0F;
    private static final float FULL_TURN = 360.0F;
    private static final float RAY_LIFT = 0.3F;
    private static final float ITEM_BOB_RATE_DIVISOR = 10.0F;
    private static final float ITEM_BOB_HEIGHT = 0.1F;
    private static final int RAY_CORE = ARGB32.color(225, 255, 248, 255);
    private static final int RAY_TIP = ARGB32.color(0, 225, 110, 235);

    private static final RenderType SPARKLE_TYPE = TTRenderTypes.SPARKLE_CULLED;

    public SpecialItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            ItemEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        if (entity.getItem().isEmpty()) {
            return;
        }

        float age = entity.getAge() + partialTicks;
        float bob = Mth.sin(age / ITEM_BOB_RATE_DIVISOR + entity.bobOffs) * ITEM_BOB_HEIGHT + ITEM_BOB_HEIGHT;
        int rays = Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FAST ? FAST_RAYS : FANCY_RAYS;
        poseStack.pushPose();
        poseStack.translate(0.0F, bob + RAY_LIFT, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(spinDegrees(age)));
        EldritchOrbRayGeometry.render(
                buffers.getBuffer(SPARKLE_TYPE), poseStack.last().pose(), rays, reach(age), RAY_CORE, RAY_TIP);
        poseStack.popPose();
    }

    static int rayCount(boolean fastGraphics) {
        return fastGraphics ? FAST_RAYS : FANCY_RAYS;
    }

    static float reach(float age) {
        return REACH * Mth.clamp(age / GROW_TICKS, 0.0F, 1.0F);
    }

    static float spinDegrees(float age) {
        return age / SPIN_TICKS_PER_TURN * FULL_TURN;
    }
}
