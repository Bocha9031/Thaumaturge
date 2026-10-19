package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeStabilizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class NodeStabilizerRenderer implements BlockEntityRenderer<BlockEntityNodeStabilizer> {
    private static final ResourceLocation MODEL = TTIds.rl("models/mesh/node_stabilizer.ttmesh");
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/block/node_stabilizer.png");
    private static final ResourceLocation OVERLAY_TEXTURE = TTIds.rl("textures/block/node_stabilizer_over.png");

    private static final RenderType BASE = RenderType.entityCutout(TEXTURE);
    private static final RenderType OVERLAY = TTRenderTypes.entityAdditiveEmissive(OVERLAY_TEXTURE);
    private static final ResourceLocation TRANSDUCER_TEXTURE = TTIds.rl("textures/block/node_converter.png");
    private static final ResourceLocation TRANSDUCER_OVERLAY_TEXTURE =
            TTIds.rl("textures/block/node_converter_over.png");
    private static final RenderType TRANSDUCER_BASE = RenderType.entityCutout(TRANSDUCER_TEXTURE);
    private static final RenderType TRANSDUCER_OVERLAY =
            TTRenderTypes.entityAdditiveEmissive(TRANSDUCER_OVERLAY_TEXTURE);
    private static final ResourceLocation BUBBLE_TEXTURE = TTIds.rl("textures/misc/node_bubble.png");
    private static final RenderType BUBBLE = TTRenderTypes.fxTranslucent(BUBBLE_TEXTURE);

    private static final String PART_LOCK = "lock";
    private static final String PART_PISTON = "piston";
    private static final int ARM_COUNT = 4;
    private static final float ARM_ANGLE_STEP = 90.0F;
    private static final float ARM_TWIST = 45.0F;
    private static final int TRANSDUCER_STATUS_NODE = 1;
    private static final int TRANSDUCER_STATUS_ENERGIZED = 2;
    private static final int TRANSDUCER_TINT_IDLE = 0xB4F2B8;
    private static final int TRANSDUCER_TINT_NODE = 0xFFA246;
    private static final int TRANSDUCER_TINT_ENERGIZED = 0xFF2E78;
    private static final int ADVANCED_TINT = 0xFF4A3C;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BUBBLE_LIGHT = LightTexture.FULL_BRIGHT;
    private static final float BUBBLE_HALF = 0.72F;
    private static final float BUBBLE_LIFT = 1.5F;
    private static final float BUBBLE_PULSE_BASE = 0.7F;
    private static final float BUBBLE_PULSE_PERIOD = 90.0F;
    private static final int BUBBLE_ADVANCED_TINT = 0xFF7A52;

    public NodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRenderOffScreen(BlockEntityNodeStabilizer stabilizer) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityNodeStabilizer stabilizer) {
        Vec3 center = Vec3.atCenterOf(stabilizer.getBlockPos()).add(0.0, BUBBLE_LIFT - 0.5, 0.0);
        double boundsHalf = BUBBLE_HALF * Math.sqrt(2.0);
        return new AABB(stabilizer.getBlockPos()).minmax(new AABB(center, center).inflate(boundsHalf));
    }

    @Override
    public void render(
            BlockEntityNodeStabilizer stabilizer,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        int count = stabilizer.count;
        boolean advanced = stabilizer.isAdvanced();
        LocalPlayer player = Minecraft.getInstance().player;
        float ticks = player == null ? 0.0F : player.tickCount + partialTick;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        submitParts(count, advanced, ticks, poseStack, buffers, light);
        poseStack.popPose();
        if (count > 0) {
            float alpha = NodeStabilizerRenderMath.bubbleAlpha(count, ticks);
            int tint = advanced ? BUBBLE_ADVANCED_TINT : 0xFFFFFF;
            int color = ARGB32.color((int) (alpha * 255.0F), tint);
            Vec3 origin = Vec3.atCenterOf(stabilizer.getBlockPos()).add(0.0, BUBBLE_LIFT - 0.5, 0.0);
            LateWorldRenderQueue.enqueueBlockEntity(
                    origin, (latePose, lateBuffers) -> drawBubble(latePose, lateBuffers, color));
        }
    }

    private static void drawBubble(PoseStack poseStack, MultiBufferSource buffers, int color) {
        poseStack.pushPose();
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = buffers.getBuffer(BUBBLE);
        buffer.addVertex(pose, -BUBBLE_HALF, -BUBBLE_HALF, 0.0F)
                .setUv(0.0F, 1.0F)
                .setColor(color)
                .setLight(BUBBLE_LIGHT);
        buffer.addVertex(pose, BUBBLE_HALF, -BUBBLE_HALF, 0.0F)
                .setUv(1.0F, 1.0F)
                .setColor(color)
                .setLight(BUBBLE_LIGHT);
        buffer.addVertex(pose, BUBBLE_HALF, BUBBLE_HALF, 0.0F)
                .setUv(1.0F, 0.0F)
                .setColor(color)
                .setLight(BUBBLE_LIGHT);
        buffer.addVertex(pose, -BUBBLE_HALF, BUBBLE_HALF, 0.0F)
                .setUv(0.0F, 0.0F)
                .setColor(color)
                .setLight(BUBBLE_LIGHT);
        poseStack.popPose();
    }

    public static void submitParts(
            int count, boolean advanced, float ticks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        TTMesh mesh = GolemMeshes.get(MODEL);
        TTMeshPart lock = findPart(mesh, PART_LOCK);
        TTMeshPart piston = findPart(mesh, PART_PISTON);
        if (lock != null) {
            GolemMeshes.renderPart(lock, poseStack.last(), buffers.getBuffer(BASE), light, WHITE);
        }
        if (piston != null) {
            for (int arm = 0; arm < ARM_COUNT; arm++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(ARM_ANGLE_STEP * arm));
                poseStack.mulPose(Axis.YP.rotationDegrees(ARM_TWIST));
                float engagement = NodeStabilizerRenderMath.engagement(count);
                poseStack.translate(0.0F, 0.0F, engagement * NodeStabilizerRenderMath.ARM_SLIDE);
                PoseStack.Pose armPose = poseStack.last();
                GolemMeshes.renderPart(piston, armPose, buffers.getBuffer(BASE), light, WHITE);
                if (engagement > 0.0F) {
                    int tint = advanced ? ADVANCED_TINT : 0xFFFFFF;
                    GolemMeshes.renderPart(
                            piston,
                            armPose,
                            buffers.getBuffer(OVERLAY),
                            LightTexture.FULL_BRIGHT,
                            NodeStabilizerRenderMath.glowColor(tint, engagement, ticks, arm));
                }
                poseStack.popPose();
            }
        }
    }

    public static void submitTransducerParts(
            float count, int status, float ticks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        TTMesh mesh = GolemMeshes.get(MODEL);
        TTMeshPart lock = findPart(mesh, PART_LOCK);
        TTMeshPart piston = findPart(mesh, PART_PISTON);
        float extension = NodeStabilizerRenderMath.transducerExtension(count);
        float glow = NodeStabilizerRenderMath.transducerGlow(extension);
        int tint =
                switch (status) {
                    case TRANSDUCER_STATUS_ENERGIZED -> TRANSDUCER_TINT_ENERGIZED;
                    case TRANSDUCER_STATUS_NODE -> TRANSDUCER_TINT_NODE;
                    default -> TRANSDUCER_TINT_IDLE;
                };
        if (lock != null) {
            PoseStack.Pose lockPose = poseStack.last();
            GolemMeshes.renderPart(lock, lockPose, buffers.getBuffer(TRANSDUCER_BASE), light, WHITE);
            GolemMeshes.renderPart(
                    lock,
                    lockPose,
                    buffers.getBuffer(TRANSDUCER_OVERLAY),
                    LightTexture.FULL_BRIGHT,
                    NodeStabilizerRenderMath.glowColor(tint, glow, ticks, ARM_COUNT));
        }
        if (piston != null) {
            for (int arm = 0; arm < ARM_COUNT; arm++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(ARM_ANGLE_STEP * arm));
                poseStack.mulPose(Axis.YP.rotationDegrees(ARM_TWIST));
                poseStack.translate(0.0F, 0.0F, extension * NodeStabilizerRenderMath.ARM_SLIDE);
                PoseStack.Pose armPose = poseStack.last();
                GolemMeshes.renderPart(piston, armPose, buffers.getBuffer(TRANSDUCER_BASE), light, WHITE);
                GolemMeshes.renderPart(
                        piston,
                        armPose,
                        buffers.getBuffer(TRANSDUCER_OVERLAY),
                        LightTexture.FULL_BRIGHT,
                        NodeStabilizerRenderMath.glowColor(tint, glow, ticks, arm));
                poseStack.popPose();
            }
        }
    }

    private static @Nullable TTMeshPart findPart(TTMesh mesh, String name) {
        for (TTMeshPart part : mesh.parts()) {
            if (name.equals(part.name())) {
                return part;
            }
        }
        return null;
    }
}
