package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.ResearchTableModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class ResearchTableInkwellPortTest {
    private static final int LIGHT = 15728880;
    private static final ResourceLocation TABLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "textures/entity/restable2.png");

    @Test
    void proceduralInkwellPartRendersOnlyWhenScribeToolsArePresent() {
        BlockEntityRendererProvider.Context context = mock(BlockEntityRendererProvider.Context.class);
        ResearchTableModel model =
                new ResearchTableModel(ResearchTableModel.createLayer().bakeRoot());
        when(context.bakeLayer(TTModelLayers.RESEARCH_TABLE)).thenReturn(model.root);
        ResearchTableRenderer renderer = new ResearchTableRenderer(context);
        MultiBufferSource buffers = mock(MultiBufferSource.class);
        VertexConsumer buffer = mock(VertexConsumer.class, RETURNS_SELF);
        when(buffers.getBuffer(any(RenderType.class))).thenReturn(buffer);

        renderer.renderInkwell(false, new PoseStack(), buffers, LIGHT);
        verifyNoInteractions(buffers);

        renderer.renderInkwell(true, new PoseStack(), buffers, LIGHT);

        assertNotNull(model.inkwell);
        verify(buffers).getBuffer(RenderType.entityCutout(TABLE_TEXTURE));
        verify(buffer, times(24))
                .addVertex(
                        anyFloat(),
                        anyFloat(),
                        anyFloat(),
                        anyInt(),
                        anyFloat(),
                        anyFloat(),
                        anyInt(),
                        anyInt(),
                        anyFloat(),
                        anyFloat(),
                        anyFloat());
    }

    @Test
    void deletedSeparateInkwellAssetsAreNotPackaged() {
        Path assets = Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets", "thaumaturge");

        assertFalse(Files.exists(assets.resolve("models/mesh/research_inkwell.ttmesh")));
        assertFalse(Files.exists(assets.resolve("textures/entity/research_inkwell.png")));
    }

    @Test
    void existingFacingTransformMatchesTheNativePoseForEveryHorizontalDirection() {
        for (Direction facing : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            PoseStack source = new PoseStack();
            BlockFacingPose.northBased(source, facing);
            source.mulPose(Axis.YP.rotationDegrees(90.0F));
            source.mulPose(Axis.XP.rotationDegrees(180.0F));

            PoseStack nativePose = new PoseStack();
            nativePose.mulPose(Axis.XP.rotationDegrees(180.0F));
            float yaw =
                    switch (facing) {
                        case NORTH -> 270.0F;
                        case SOUTH -> 90.0F;
                        case WEST -> 180.0F;
                        default -> 0.0F;
                    };
            nativePose.mulPose(Axis.YP.rotationDegrees(yaw));

            for (float[] point : List.of(
                    new float[] {1.0F, 0.0F, 0.0F}, new float[] {0.0F, 1.0F, 0.0F}, new float[] {0.0F, 0.0F, 1.0F})) {
                Vector3f sourceResult =
                        source.last().pose().transformPosition(point[0], point[1], point[2], new Vector3f());
                Vector3f nativeResult =
                        nativePose.last().pose().transformPosition(point[0], point[1], point[2], new Vector3f());
                assertEquals(sourceResult.x, nativeResult.x, 1.0E-6F, facing.toString());
                assertEquals(sourceResult.y, nativeResult.y, 1.0E-6F, facing.toString());
                assertEquals(sourceResult.z, nativeResult.z, 1.0E-6F, facing.toString());
            }
        }
    }
}
