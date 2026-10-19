package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.model.entity.GrapplerModel;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.entity.projectile.EntityGrapple;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class GrappleRenderer extends EntityRenderer<EntityGrapple> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/grappler.png");
    private static final ResourceLocation ROPE = TTIds.rl("textures/misc/rope.png");
    private static final RenderType ROPE_TYPE = TTRenderTypes.fxTranslucent(ROPE);
    private static final RenderType GLOW_TYPE = TTRenderTypes.fxAdditiveBlurred(ParticleTextures.ORB_GLOW);

    private static final double ROPE_RADIUS = 0.022;
    private static final int ROPE_SIDES = 6;
    private static final double VERTICAL_THRESHOLD = 0.9;
    private static final double TANGENT_EPSILON = 1.0E-8;
    private static final float ROPE_V_STEP = 0.5F;
    private static final int EMISSIVE_LIGHT = 0x00F000DC;
    private static final float GLOW_HALF = 0.5F;
    private static final float GLOW_PULSE_TICKS = 24.0F;
    private static final float GLOW_BASE_SCALE = 0.45F;
    private static final float GLOW_PULSE_SCALE = 0.08F;
    private static final float HAND_SIDE = 0.32F;
    private static final float HAND_FORWARD = 0.55F;
    private static final float HAND_DROP = 0.28F;
    private static final float BODY_MIDDLE = 0.5F;
    private final GrapplerModel model;

    public GrappleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new GrapplerModel(context.bakeLayer(TTModelLayers.GRAPPLER));
    }

    @Override
    public void render(
            EntityGrapple entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float age = entity.tickCount + partialTicks;
        int ticks = entity.tickCount;
        List<Vec3> points = new ArrayList<>();
        calcPoints(entity.getOwner(), entity, partialTicks, age, points);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        model.root.render(
                poseStack, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        int frame = Math.floorMod(ticks, ParticleTextures.ORB_GLOW_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.ORB_GLOW_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.ORB_GLOW_FRAMES);
        float v0 = StripUv.V0;
        float v1 = StripUv.V1;
        float glowScale = GLOW_BASE_SCALE + GLOW_PULSE_SCALE * Mth.sin(age / GLOW_PULSE_TICKS * Mth.TWO_PI);
        int glowTint = ARGB32.colorFromFloat(0.35F, 0.94F, 0.94F, 1.0F);
        Vec3 glowOrigin = entity.getPosition(partialTicks);
        LateWorldRenderQueue.enqueue(glowOrigin, (latePose, lateBuffers) -> {
            latePose.mulPose(this.entityRenderDispatcher.cameraOrientation());
            latePose.scale(glowScale, glowScale, glowScale);
            writeGlow(lateBuffers.getBuffer(GLOW_TYPE), latePose.last().pose(), GLOW_HALF, u0, v0, u1, v1, glowTint);
        });
        if (points.size() > 2) {
            submitRope(buffers.getBuffer(ROPE_TYPE), poseStack.last().pose(), points, packedLight);
        }
    }

    private static void writeGlow(
            VertexConsumer glowBuffer,
            Matrix4f glowMat,
            float half,
            float u0,
            float v0,
            float u1,
            float v1,
            int glowTint) {
        glowBuffer
                .addVertex(glowMat, -half, -half, 0.0F)
                .setUv(u0, v1)
                .setColor(glowTint)
                .setLight(EMISSIVE_LIGHT);
        glowBuffer
                .addVertex(glowMat, half, -half, 0.0F)
                .setUv(u1, v1)
                .setColor(glowTint)
                .setLight(EMISSIVE_LIGHT);
        glowBuffer
                .addVertex(glowMat, half, half, 0.0F)
                .setUv(u1, v0)
                .setColor(glowTint)
                .setLight(EMISSIVE_LIGHT);
        glowBuffer
                .addVertex(glowMat, -half, half, 0.0F)
                .setUv(u0, v0)
                .setColor(glowTint)
                .setLight(EMISSIVE_LIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGrapple entity) {
        return TEXTURE;
    }

    private static void submitRope(VertexConsumer buffer, Matrix4f mat, List<Vec3> points, int light) {
        Vector3f[] rings = buildRings(points);
        for (int index = 0; index < points.size() - 1; index++) {
            float v0 = index * ROPE_V_STEP;
            float v1 = (index + 1) * ROPE_V_STEP;
            for (int side = 0; side < ROPE_SIDES; side++) {
                int nextSide = (side + 1) % ROPE_SIDES;
                float u0 = side / (float) ROPE_SIDES;
                float u1 = (side + 1) / (float) ROPE_SIDES;
                addRopeVertex(buffer, mat, rings[index * ROPE_SIDES + side], u0, v0, light);
                addRopeVertex(buffer, mat, rings[(index + 1) * ROPE_SIDES + side], u0, v1, light);
                addRopeVertex(buffer, mat, rings[(index + 1) * ROPE_SIDES + nextSide], u1, v1, light);
                addRopeVertex(buffer, mat, rings[index * ROPE_SIDES + nextSide], u1, v0, light);
            }
        }
    }

    private static void addRopeVertex(VertexConsumer buffer, Matrix4f mat, Vector3f pos, float u, float v, int light) {
        buffer.addVertex(mat, pos.x(), pos.y(), pos.z())
                .setUv(u, v)
                .setColor(-1)
                .setLight(light);
    }

    static Vector3f[] buildRings(List<Vec3> points) {
        Vector3f[] rings = new Vector3f[points.size() * ROPE_SIDES];
        double nx = 0.0;
        double ny = 0.0;
        double nz = 0.0;
        double tx = 0.0;
        double ty = 1.0;
        double tz = 0.0;
        for (int index = 0; index < points.size(); index++) {
            Vec3 center = points.get(index);
            Vec3 before = points.get(Math.max(0, index - 1));
            Vec3 after = points.get(Math.min(points.size() - 1, index + 1));
            double dx = after.x - before.x;
            double dy = after.y - before.y;
            double dz = after.z - before.z;
            double tangentLength = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (tangentLength > TANGENT_EPSILON) {
                tx = dx / tangentLength;
                ty = dy / tangentLength;
                tz = dz / tangentLength;
            }
            boolean steep = Math.abs(ty) > VERTICAL_THRESHOLD;
            if (index == 0) {
                nx = steep ? 1.0 : 0.0;
                ny = steep ? 0.0 : 1.0;
                nz = 0.0;
            }
            double normalAlongTangent = nx * tx + ny * ty + nz * tz;
            nx -= normalAlongTangent * tx;
            ny -= normalAlongTangent * ty;
            nz -= normalAlongTangent * tz;
            double normalLength = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (normalLength < TANGENT_EPSILON) {
                nx = steep ? 1.0 - tx * tx : -ty * tx;
                ny = steep ? -tx * ty : 1.0 - ty * ty;
                nz = steep ? -tx * tz : -ty * tz;
                normalLength = Math.sqrt(nx * nx + ny * ny + nz * nz);
            }
            nx /= normalLength;
            ny /= normalLength;
            nz /= normalLength;
            double bx = ty * nz - tz * ny;
            double by = tz * nx - tx * nz;
            double bz = tx * ny - ty * nx;
            for (int side = 0; side < ROPE_SIDES; side++) {
                double angle = side * Mth.TWO_PI / ROPE_SIDES;
                double cos = Math.cos(angle) * ROPE_RADIUS;
                double sin = Math.sin(angle) * ROPE_RADIUS;
                rings[index * ROPE_SIDES + side] = new Vector3f(
                        (float) (center.x + nx * cos + bx * sin), (float) (center.y + ny * cos + by * sin), (float)
                                (center.z + nz * cos + bz * sin));
            }
        }
        return rings;
    }

    private static void calcPoints(
            Entity thrower, EntityGrapple grapple, float partialTicks, float age, List<Vec3> points) {
        if (thrower == null) {
            return;
        }
        Vec3 end = ropeEnd(grapple, thrower, partialTicks).subtract(grapple.getPosition(partialTicks));
        points.addAll(GrappleRopeMath.points(end, grapple.ampl, age));
    }

    private static Vec3 ropeEnd(EntityGrapple grapple, Entity owner, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (owner == minecraft.getCameraEntity()
                && minecraft.options.getCameraType().isFirstPerson()) {
            Vec3 eye = owner.getEyePosition(partialTicks);
            Vec3 look = owner.getViewVector(partialTicks);
            Vec3 right = new Vec3(-look.z, 0.0, look.x);
            double rightLength = right.length();
            right = rightLength > TANGENT_EPSILON ? right.scale(1.0 / rightLength) : new Vec3(1.0, 0.0, 0.0);
            Vec3 down = look.cross(right);
            double side = grapple.getHand() == InteractionHand.MAIN_HAND ? HAND_SIDE : -HAND_SIDE;
            return eye.add(look.scale(HAND_FORWARD)).add(right.scale(side)).add(down.scale(HAND_DROP));
        }
        return owner.getPosition(partialTicks).add(0.0, owner.getBbHeight() * BODY_MIDDLE, 0.0);
    }
}
