package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.tube.buffer.ChokeLevel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class TubeBufferRenderer implements BlockEntityRenderer<BlockEntityTubeBuffer, TubeBufferRenderState> {
    private static final Identifier TUBE_SPRITE = TTIds.rl("block/tube_2");
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float BAND_NEAR = 2.0F * PIXEL;
    private static final float BAND_FAR = 3.0F * PIXEL;
    private static final float COLLAR_LOW = 5.875F * PIXEL;
    private static final float COLLAR_HIGH = 10.125F * PIXEL;
    private static final float U_START = 11.0F * PIXEL;
    private static final float U_END = 12.0F * PIXEL;
    private static final float V_START = 5.0F * PIXEL;
    private static final float V_END = 7.0F * PIXEL;
    private static final int REDUCED_COLOR = 0xFF7895B2;
    private static final int BLOCKED_COLOR = 0xFFC4867D;
    private static final int NO_BAND = 0;
    private static final int AXES = 3;
    private static final Direction[] DIRECTIONS = Direction.values();

    public TubeBufferRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public TubeBufferRenderState createRenderState() {
        return new TubeBufferRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityTubeBuffer buffer, TubeBufferRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(buffer, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = buffer.getBlockState();
        state.hasBands = false;
        for (Direction side : DIRECTIONS) {
            int color = blockState.getValue(BlockEssentiaTransport.propertyFor(side)) ? bandColor(ChokeLevel.fromOrdinal(buffer.chokedSide(side))) : NO_BAND;
            state.bandColors[side.ordinal()] = color;
            state.hasBands |= color != NO_BAND;
        }
    }

    private static int bandColor(ChokeLevel level) {
        return switch (level) {
            case REDUCED -> REDUCED_COLOR;
            case BLOCKED -> BLOCKED_COLOR;
            case NORMAL -> NO_BAND;
        };
    }

    @Override
    public void submit(TubeBufferRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.hasBands) {
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(TUBE_SPRITE);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
            for (Direction side : DIRECTIONS) {
                int color = state.bandColors[side.ordinal()];
                if (color != NO_BAND) {
                    band(pose, buffer, side, color, light, sprite);
                }
            }
        });
    }

    private static void band(PoseStack.Pose pose, VertexConsumer buffer, Direction side, int color, int light, TextureAtlasSprite sprite) {
        float[] min = {COLLAR_LOW, COLLAR_LOW, COLLAR_LOW};
        float[] max = {COLLAR_HIGH, COLLAR_HIGH, COLLAR_HIGH};
        int axis = side.getAxis().ordinal();
        boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        min[axis] = positive ? 1.0F - BAND_FAR : BAND_NEAR;
        max[axis] = positive ? 1.0F - BAND_NEAR : BAND_FAR;
        for (Direction face : DIRECTIONS) {
            if (face.getAxis() != side.getAxis()) {
                face(pose, buffer, face, min, max, color, light, sprite);
            }
        }
    }

    private static void face(PoseStack.Pose pose, VertexConsumer buffer, Direction face, float[] min, float[] max, int color, int light, TextureAtlasSprite sprite) {
        int axis = face.getAxis().ordinal();
        int uAxis = (axis + 1) % AXES;
        int vAxis = (axis + 2) % AXES;
        float fixed = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? max[axis] : min[axis];
        float[] point = new float[AXES];
        float[][] corners = {{min[uAxis], min[vAxis], U_START, V_END}, {max[uAxis], min[vAxis], U_END, V_END}, {max[uAxis], max[vAxis], U_END, V_START}, {min[uAxis], max[vAxis], U_START, V_START}};
        for (float[] corner : corners) {
            point[axis] = fixed;
            point[uAxis] = corner[0];
            point[vAxis] = corner[1];
            buffer.addVertex(pose, point[0], point[1], point[2]).setColor(color).setUv(sprite.getU(corner[2]), sprite.getV(corner[3])).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(pose, face.getStepX(), face.getStepY(), face.getStepZ());
        }
    }
}
