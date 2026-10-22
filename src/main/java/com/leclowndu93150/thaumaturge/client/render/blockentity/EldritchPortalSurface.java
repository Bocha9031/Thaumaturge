package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4fc;

public final class EldritchPortalSurface {
    private static final int UV_WRAP = 1024;

    public static final RenderType SURFACE = RenderType.create("tc_eldritch_portal_surface", RenderSetup.builder(TTRenderPipelines.PORTAL_SURFACE).createRenderSetup());

    private EldritchPortalSurface() {}

    public static void quad(PoseStack.Pose pose, VertexConsumer buffer, BlockPos worldPos, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        Matrix4fc mat = pose.pose();
        float e1x = x2 - x1;
        float e1y = y2 - y1;
        float e1z = z2 - z1;
        float e2x = x3 - x1;
        float e2y = y3 - y1;
        float e2z = z3 - z1;
        float nx = Math.abs(e1y * e2z - e1z * e2y);
        float ny = Math.abs(e1z * e2x - e1x * e2z);
        float nz = Math.abs(e1x * e2y - e1y * e2x);
        int axis = ny >= nx && ny >= nz ? 1 : nx >= nz ? 0 : 2;
        addVertex(buffer, mat, worldPos, axis, x1, y1, z1);
        addVertex(buffer, mat, worldPos, axis, x2, y2, z2);
        addVertex(buffer, mat, worldPos, axis, x3, y3, z3);
        addVertex(buffer, mat, worldPos, axis, x4, y4, z4);
    }

    private static void addVertex(VertexConsumer buffer, Matrix4fc mat, BlockPos worldPos, int axis, float x, float y, float z) {
        float wx = Math.floorMod(worldPos.getX(), UV_WRAP) + x;
        float wy = Math.floorMod(worldPos.getY(), UV_WRAP) + y;
        float wz = Math.floorMod(worldPos.getZ(), UV_WRAP) + z;
        float u;
        float v;
        if (axis == 1) {
            u = wx;
            v = wz;
        } else if (axis == 0) {
            u = wz;
            v = wy;
        } else {
            u = wx;
            v = wy;
        }
        buffer.addVertex(mat, x, y, z).setUv(u, v);
    }
}
