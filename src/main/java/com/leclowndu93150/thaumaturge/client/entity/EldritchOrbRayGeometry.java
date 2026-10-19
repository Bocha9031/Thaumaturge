package com.leclowndu93150.thaumaturge.client.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

final class EldritchOrbRayGeometry {
    private static final int MAX_RAYS = 16;
    private static final int CORNERS_PER_TIP = 3;
    private static final int COMPONENTS = 3;
    private static final int RAY_STRIDE = CORNERS_PER_TIP * COMPONENTS;
    private static final float SHORTEST_RAY = 0.2F;
    private static final float THINNEST_RAY = 0.035F;
    private static final float WIDTH_SPREAD = 0.05F;
    private static final double SPREAD_STEP_U = 0.7548776662466927;
    private static final double SPREAD_STEP_V = 0.5698402909980532;
    private static final double LENGTH_STEP = 0.6180339887498949;
    private static final double WIDTH_STEP = 0.41421356237309515;
    private static final double LENGTH_SEED = 0.31;
    private static final double WIDTH_SEED = 0.17;
    private static final double VERTICAL_THRESHOLD = 0.9;
    private static final double TWO_PI = Math.PI * 2.0;

    private static final float[] TIPS = buildTips();

    private EldritchOrbRayGeometry() {}

    static void render(VertexConsumer buffer, Matrix4f matrix, int rayCount, float reach, int coreColor, int tipColor) {
        int count = Math.clamp(rayCount, 0, MAX_RAYS);
        if (count == 0 || reach <= 0.0F) {
            return;
        }
        for (int ray = 0; ray < count; ray++) {
            int base = ray * RAY_STRIDE;
            for (int corner = 0; corner < CORNERS_PER_TIP; corner++) {
                int from = base + corner * COMPONENTS;
                int to = base + (corner + 1) % CORNERS_PER_TIP * COMPONENTS;
                buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(coreColor);
                buffer.addVertex(matrix, TIPS[to] * reach, TIPS[to + 1] * reach, TIPS[to + 2] * reach)
                        .setColor(tipColor);
                buffer.addVertex(matrix, TIPS[from] * reach, TIPS[from + 1] * reach, TIPS[from + 2] * reach)
                        .setColor(tipColor);
            }
        }
    }

    static float[] tipsForTest() {
        return TIPS.clone();
    }

    private static float[] buildTips() {
        float[] tips = new float[MAX_RAYS * RAY_STRIDE];
        for (int ray = 0; ray < MAX_RAYS; ray++) {
            double u = fraction(0.5 + ray * SPREAD_STEP_U);
            double v = fraction(0.5 + ray * SPREAD_STEP_V);
            double dy = 1.0 - 2.0 * v;
            double ring = Math.sqrt(Math.max(0.0, 1.0 - dy * dy));
            double angle = u * TWO_PI;
            double dx = Math.cos(angle) * ring;
            double dz = Math.sin(angle) * ring;
            double length = SHORTEST_RAY + (1.0 - SHORTEST_RAY) * fraction(LENGTH_SEED + ray * LENGTH_STEP);
            double width = THINNEST_RAY + WIDTH_SPREAD * fraction(WIDTH_SEED + ray * WIDTH_STEP);
            double upX = Math.abs(dy) < VERTICAL_THRESHOLD ? 0.0 : 1.0;
            double upY = Math.abs(dy) < VERTICAL_THRESHOLD ? 1.0 : 0.0;
            double along = upX * dx + upY * dy;
            double px = upX - along * dx;
            double py = upY - along * dy;
            double pz = -along * dz;
            double perpendicularLength = Math.sqrt(px * px + py * py + pz * pz);
            px /= perpendicularLength;
            py /= perpendicularLength;
            pz /= perpendicularLength;
            double qx = dy * pz - dz * py;
            double qy = dz * px - dx * pz;
            double qz = dx * py - dy * px;
            for (int corner = 0; corner < CORNERS_PER_TIP; corner++) {
                double turn = corner * TWO_PI / CORNERS_PER_TIP;
                double cos = Math.cos(turn) * width;
                double sin = Math.sin(turn) * width;
                int at = ray * RAY_STRIDE + corner * COMPONENTS;
                tips[at] = (float) (dx * length + px * cos + qx * sin);
                tips[at + 1] = (float) (dy * length + py * cos + qy * sin);
                tips[at + 2] = (float) (dz * length + pz * cos + qz * sin);
            }
        }
        return tips;
    }

    private static double fraction(double value) {
        return value - Math.floor(value);
    }
}
