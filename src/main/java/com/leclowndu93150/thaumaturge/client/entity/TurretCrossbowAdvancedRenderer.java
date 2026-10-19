package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbowAdvanced;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public final class TurretCrossbowAdvancedRenderer extends EntityRenderer<EntityTurretCrossbowAdvanced> {
    private static final ResourceLocation MODEL = TTIds.rl("models/mesh/crossbow_advanced.ttmesh");
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/crossbow_advanced.png");
    private static final String LEGS = "legs";
    private static final String LOADER = "loader";
    private static final String BOW_RIGHT = "bow1";
    private static final String BOW_LEFT = "bow2";
    private static final float SHADOW = 0.5F;
    private static final float MESH_SCALE = 0.9F;
    private static final float STAND_HEIGHT = 0.765F;
    private static final float CART_LEG_REACH = 0.65F;
    private static final float CART_LEG_LENGTH = 0.45F;
    private static final float HURT_JIGGLE = 0.035F;
    private static final float LOADER_TRAVEL = 0.14F;
    private static final float BOW_PIVOT_Y = 0.094F;
    private static final float BOW_PIVOT_Z = 0.06F;
    private static final float BOW_SWING_DEGREES = 28.0F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int HURT_TINT = 0xFFFFB0AA;

    public TurretCrossbowAdvancedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW;
    }

    @Override
    public void render(
            EntityTurretCrossbowAdvanced entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        TTMesh mesh = GolemMeshes.get(MODEL);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        boolean ridingMinecart = entity.getVehicle() instanceof AbstractMinecart;
        float headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float loadProgress = entity.getLoadProgress(partialTicks);
        float swingAnim = entity.swingAnim;
        float hurtFade = TurretCrossbowMotion.hurtFade(entity.hurtTime, partialTicks, entity.hurtDuration);
        float jiggleX = 0.0F;
        float jiggleY = 0.0F;
        float jiggleZ = 0.0F;
        if (hurtFade > 0.0F) {
            float strength = HURT_JIGGLE * hurtFade;
            jiggleX = (entity.getRandom().nextFloat() * 2.0F - 1.0F) * strength;
            jiggleY = (entity.getRandom().nextFloat() * 2.0F - 1.0F) * strength;
            jiggleZ = (entity.getRandom().nextFloat() * 2.0F - 1.0F) * strength;
        }
        int upperTint = hurtFade > 0.0F ? HURT_TINT : WHITE;
        float legLength = ridingMinecart ? CART_LEG_LENGTH : 1.0F;
        float legReach = ridingMinecart ? CART_LEG_REACH : 1.0F;
        float loaderShift = LOADER_TRAVEL * TurretCrossbowMotion.snap(loadProgress);
        float bowSwing = BOW_SWING_DEGREES * TurretCrossbowMotion.snap(swingAnim);
        poseStack.pushPose();
        poseStack.translate(0.0F, STAND_HEIGHT * MESH_SCALE * legLength, 0.0F);
        poseStack.scale(MESH_SCALE, MESH_SCALE, MESH_SCALE);
        for (TTMeshPart part : mesh.parts()) {
            poseStack.pushPose();
            if (LEGS.equals(part.name())) {
                poseStack.scale(legReach, legLength, legReach);
                GolemMeshes.renderPart(part, poseStack.last(), buffer, packedLight, WHITE);
            } else {
                poseStack.translate(jiggleX, jiggleY, jiggleZ);
                poseStack.mulPose(Axis.YN.rotationDegrees(headYaw));
                poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));
                placeMovingPart(poseStack, part.name(), bowSwing, loaderShift);
                GolemMeshes.renderPart(part, poseStack.last(), buffer, packedLight, upperTint);
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTurretCrossbowAdvanced entity) {
        return TEXTURE;
    }

    private static void placeMovingPart(PoseStack poseStack, String name, float bowSwing, float loaderShift) {
        if (LOADER.equals(name)) {
            poseStack.translate(0.0F, 0.0F, loaderShift);
        } else if (BOW_RIGHT.equals(name)) {
            swingBow(poseStack, bowSwing);
        } else if (BOW_LEFT.equals(name)) {
            swingBow(poseStack, -bowSwing);
        }
    }

    private static void swingBow(PoseStack poseStack, float degrees) {
        poseStack.translate(0.0F, BOW_PIVOT_Y, BOW_PIVOT_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        poseStack.translate(0.0F, -BOW_PIVOT_Y, -BOW_PIVOT_Z);
    }
}
