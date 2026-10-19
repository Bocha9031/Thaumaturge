package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class EldritchOrbRenderer extends EntityRenderer<EntityEldritchOrb> {
    private static final RenderType RAY_TYPE = TTRenderTypes.SPARKLE_CULLED;
    private static final RenderType BILLBOARD_TYPE = TTRenderTypes.fxAdditive(ParticleTextures.ELDRITCH_ORB);

    private static final int RAYS = 12;
    private static final float CORE_HALF = 0.375F;
    private static final float CORE_FLICKER_BASE = 0.8F;
    private static final float CORE_FLICKER_DEPTH = 0.2F;
    private static final float CORE_FLICKER_RATE = 1.7F;
    private static final float REACH = 0.9F;
    private static final float GROW_TICKS = 10.0F;
    private static final float SPIN_TICKS_PER_TURN = 90.0F;
    private static final float FULL_TURN = 360.0F;
    private static final int RAY_CORE = ARGB32.color(235, 255, 255, 255);
    private static final int RAY_TIP = ARGB32.color(110, 150, 148, 160);
    private static final int EMISSIVE_LIGHT = 0x00F000F0;

    public EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            EntityEldritchOrb entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        float age = entity.tickCount + partialTicks;
        float centre = entity.getBbHeight() * 0.5F;
        float grow = Mth.clamp(age / GROW_TICKS, 0.0F, 1.0F);
        float spin = age / SPIN_TICKS_PER_TURN * FULL_TURN;
        poseStack.pushPose();
        poseStack.translate(0.0F, centre, 0.0F);
        EldritchOrbRayGeometry.render(
                buffers.getBuffer(RAY_TYPE), poseStack.last().pose(), RAYS, REACH * grow, RAY_CORE, RAY_TIP);
        poseStack.popPose();
        int frame = Math.floorMod(Mth.floor(age), ParticleTextures.ELDRITCH_ORB_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.ELDRITCH_ORB_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.ELDRITCH_ORB_FRAMES);
        float brightness =
                Mth.clamp(CORE_FLICKER_BASE + CORE_FLICKER_DEPTH * Mth.sin(age * CORE_FLICKER_RATE), 0.0F, 1.0F);
        int tint = ARGB32.colorFromFloat(1.0F, brightness, brightness, brightness);
        Vec3 origin = entity.getPosition(partialTicks).add(0.0, centre, 0.0);
        LateWorldRenderQueue.enqueue(origin, (latePose, lateBuffers) -> {
            latePose.mulPose(this.entityRenderDispatcher.cameraOrientation());
            writeBillboard(
                    lateBuffers.getBuffer(BILLBOARD_TYPE), latePose.last().pose(), u0, u1, tint);
        });
    }

    private static void writeBillboard(VertexConsumer buffer, Matrix4f mat, float u0, float u1, int tint) {
        buffer.addVertex(mat, -CORE_HALF, -CORE_HALF, 0.0F)
                .setUv(u0, StripUv.V1)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, CORE_HALF, -CORE_HALF, 0.0F)
                .setUv(u1, StripUv.V1)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, CORE_HALF, CORE_HALF, 0.0F)
                .setUv(u1, StripUv.V0)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
        buffer.addVertex(mat, -CORE_HALF, CORE_HALF, 0.0F)
                .setUv(u0, StripUv.V0)
                .setColor(tint)
                .setLight(EMISSIVE_LIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEldritchOrb entity) {
        return ParticleTextures.ELDRITCH_ORB;
    }
}
