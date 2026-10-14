package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class AlembicRenderer implements BlockEntityRenderer<BlockEntityAlembic> {

    private static final ResourceLocation LABEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "textures/entity/label.png");

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityAlembic alembic,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        Level level = alembic.getLevel();
        if (level == null || alembic.aspectFilterKey() == null) {
            return;
        }
        Holder<IAspect> filter = EssentiaTransportHelper.resolve(level.registryAccess(), alembic.aspectFilterKey());
        if (filter == null) {
            return;
        }
        ResourceLocation filterTexture = filter.value().texture();
        int filterColor = 0xFF000000 | (filter.value().color() & 0x00FFFFFF);
        submitFilterLabel(alembic.spoutSide(), filterTexture, filterColor, poseStack, buffers, light);
    }

    public static void addVertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color,
            int light,
            float nx,
            float ny,
            float nz) {
        buffer.addVertex(pose, x, y, z)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz)
                .setColor(color);
    }

    private void submitFilterLabel(
            Direction facing,
            ResourceLocation filterTexture,
            int filterColor,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if (facing.getAxis() == Direction.Axis.Z) poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        poseStack.translate(0.0, 0.0, -0.376f);
        labelQuad(buffers.getBuffer(RenderType.entityCutout(LABEL_TEXTURE)), poseStack.last(), light);
        if (filterTexture != null) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(0.0, 0.0, 0.001);
            aspectIconQuad(
                    buffers.getBuffer(RenderType.entityTranslucent(filterTexture)),
                    poseStack.last(),
                    filterColor,
                    light);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void labelQuad(VertexConsumer buffer, PoseStack.Pose pose, int light) {
        float s = 0.18F;
        int white = 0xFFFFFFFF;
        addVertex(buffer, pose, -s, -s, 0.0F, 0.0F, 1.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, -s, 0.0F, 1.0F, 1.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, s, 0.0F, 1.0F, 0.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, -s, s, 0.0F, 0.0F, 0.0F, white, light, 0.0F, 0.0F, -1.0F);
    }

    private static void aspectIconQuad(VertexConsumer buffer, PoseStack.Pose pose, int color, int light) {
        float s = 0.12F;
        addVertex(buffer, pose, -s, -s, 0.0F, 0.0F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, -s, 0.0F, 1.0F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, s, 0.0F, 1.0F, 0.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, -s, s, 0.0F, 0.0F, 0.0F, color, light, 0.0F, 0.0F, -1.0F);
    }
}
