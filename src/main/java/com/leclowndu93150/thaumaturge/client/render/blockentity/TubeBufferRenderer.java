package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.tube.buffer.ChokeLevel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;

public final class TubeBufferRenderer implements BlockEntityRenderer<BlockEntityTubeBuffer> {
    private static final ResourceLocation TUBE_TEXTURE = TTIds.rl("block/tube_2");
    private static final float BAND_START = 2.0F / 16.0F;
    private static final float BAND_END = 3.0F / 16.0F;
    private static final float COLLAR_LOW = 5.875F / 16.0F;
    private static final float COLLAR_HIGH = 10.125F / 16.0F;
    // Reuse a narrow, opaque crop from the pipe-shaft UVs in tube_side.json.
    private static final float TEXTURE_U_START = 11.0F / 16.0F;
    private static final float TEXTURE_U_END = 12.0F / 16.0F;
    private static final float TEXTURE_V_START = 5.0F / 16.0F;
    private static final float TEXTURE_V_END = 7.0F / 16.0F;
    private static final int REDUCED_COLOR = 0xFF7895B2;
    private static final int BLOCKED_COLOR = 0xFFC4867D;
    private static final int NORMAL_COLOR = 0xFFFFFFFF;

    public TubeBufferRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityTubeBuffer buffer,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BlockState state = buffer.getBlockState();
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(TUBE_TEXTURE);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose pose = poseStack.last();
        float uStart = sprite.getU(TEXTURE_U_START);
        float uEnd = sprite.getU(TEXTURE_U_END);
        float vStart = sprite.getV(TEXTURE_V_START);
        float vEnd = sprite.getV(TEXTURE_V_END);

        for (Direction side : Direction.values()) {
            if (!state.getValue(BlockEssentiaTransport.propertyFor(side))) {
                continue;
            }
            int color =
                    switch (ChokeLevel.fromOrdinal(buffer.chokedSide(side))) {
                        case REDUCED -> REDUCED_COLOR;
                        case BLOCKED -> BLOCKED_COLOR;
                        case NORMAL -> NORMAL_COLOR;
                    };
            if (color == NORMAL_COLOR) {
                continue;
            }
            renderBand(consumer, pose, side, color, light, uStart, uEnd, vStart, vEnd);
        }
    }

    private static void renderBand(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            Direction side,
            int color,
            int light,
            float uStart,
            float uEnd,
            float vStart,
            float vEnd) {
        float axialStart =
                switch (side) {
                    case DOWN, NORTH, WEST -> BAND_START;
                    case UP, SOUTH, EAST -> 1.0F - BAND_END;
                };
        float axialEnd =
                switch (side) {
                    case DOWN, NORTH, WEST -> BAND_END;
                    case UP, SOUTH, EAST -> 1.0F - BAND_START;
                };
        face(
                buffer,
                pose,
                point(side, axialStart, COLLAR_LOW, COLLAR_LOW),
                point(side, axialEnd, COLLAR_LOW, COLLAR_LOW),
                point(side, axialEnd, COLLAR_LOW, COLLAR_HIGH),
                point(side, axialStart, COLLAR_LOW, COLLAR_HIGH),
                crossANormal(side, -1.0F),
                color,
                light,
                uStart,
                uEnd,
                vStart,
                vEnd);
        face(
                buffer,
                pose,
                point(side, axialStart, COLLAR_HIGH, COLLAR_HIGH),
                point(side, axialEnd, COLLAR_HIGH, COLLAR_HIGH),
                point(side, axialEnd, COLLAR_HIGH, COLLAR_LOW),
                point(side, axialStart, COLLAR_HIGH, COLLAR_LOW),
                crossANormal(side, 1.0F),
                color,
                light,
                uStart,
                uEnd,
                vStart,
                vEnd);
        face(
                buffer,
                pose,
                point(side, axialStart, COLLAR_LOW, COLLAR_LOW),
                point(side, axialEnd, COLLAR_LOW, COLLAR_LOW),
                point(side, axialEnd, COLLAR_HIGH, COLLAR_LOW),
                point(side, axialStart, COLLAR_HIGH, COLLAR_LOW),
                crossBNormal(side, -1.0F),
                color,
                light,
                uStart,
                uEnd,
                vStart,
                vEnd);
        face(
                buffer,
                pose,
                point(side, axialStart, COLLAR_HIGH, COLLAR_HIGH),
                point(side, axialEnd, COLLAR_HIGH, COLLAR_HIGH),
                point(side, axialEnd, COLLAR_LOW, COLLAR_HIGH),
                point(side, axialStart, COLLAR_LOW, COLLAR_HIGH),
                crossBNormal(side, 1.0F),
                color,
                light,
                uStart,
                uEnd,
                vStart,
                vEnd);
    }

    private static float[] point(Direction side, float axial, float crossA, float crossB) {
        return switch (side.getAxis()) {
            case X -> new float[] {axial, crossA, crossB};
            case Y -> new float[] {crossA, axial, crossB};
            case Z -> new float[] {crossA, crossB, axial};
        };
    }

    private static float[] crossANormal(Direction side, float sign) {
        return switch (side.getAxis()) {
            case X -> new float[] {0.0F, sign, 0.0F};
            case Y, Z -> new float[] {sign, 0.0F, 0.0F};
        };
    }

    private static float[] crossBNormal(Direction side, float sign) {
        return switch (side.getAxis()) {
            case X, Y -> new float[] {0.0F, 0.0F, sign};
            case Z -> new float[] {0.0F, sign, 0.0F};
        };
    }

    private static void face(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float[] first,
            float[] second,
            float[] third,
            float[] fourth,
            float[] normal,
            int color,
            int light,
            float uStart,
            float uEnd,
            float vStart,
            float vEnd) {
        vertex(buffer, pose, first, uStart, vEnd, color, light, normal);
        vertex(buffer, pose, second, uEnd, vEnd, color, light, normal);
        vertex(buffer, pose, third, uEnd, vStart, color, light, normal);
        vertex(buffer, pose, fourth, uStart, vStart, color, light, normal);
    }

    private static void vertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float[] point,
            float u,
            float v,
            int color,
            int light,
            float[] normal) {
        buffer.addVertex(pose, point[0], point[1], point[2])
                .setUv(u, v)
                .setColor(color)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, normal[0], normal[1], normal[2]);
    }
}
