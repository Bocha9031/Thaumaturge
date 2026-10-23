package com.leclowndu93150.thaumaturge.client.render;

import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class PortalStrip {
    private static final int LIGHT = 0x00F000DC;

    private PortalStrip() {}

    public static void write(PoseStack.Pose pose, VertexConsumer buffer, float halfWidth, float halfHeight, int frame, int frameCount, int tint) {
        float uMin = StripUv.u0(frame, frameCount);
        float uMax = StripUv.u1(frame, frameCount);
        vertex(pose, buffer, -halfWidth, -halfHeight, uMax, StripUv.V0, tint);
        vertex(pose, buffer, -halfWidth, halfHeight, uMax, StripUv.V1, tint);
        vertex(pose, buffer, halfWidth, halfHeight, uMin, StripUv.V1, tint);
        vertex(pose, buffer, halfWidth, -halfHeight, uMin, StripUv.V0, tint);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float u, float v, int tint) {
        buffer.addVertex(pose, x, y, 0.0F).setUv(u, v).setColor(tint).setLight(LIGHT);
    }
}
