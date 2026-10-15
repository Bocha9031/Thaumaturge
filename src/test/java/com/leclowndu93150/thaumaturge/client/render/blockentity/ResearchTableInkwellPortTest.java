package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshLoader;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ResearchTableInkwellPortTest {
    private static final ResourceLocation MESH_ID =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "models/mesh/research_inkwell.ttmesh");
    private static final ResourceLocation TEXTURE_ID =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "textures/entity/research_inkwell.png");
    private static final int LIGHT = 15728880;
    private static final int WHITE = 0xFFFFFFFF;

    @Test
    void sourceInkwellMeshAndTextureLoadWithAllMaterialParts() throws Exception {
        Path assets = Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets");
        ResourceManager resources = mock(ResourceManager.class);
        when(resources.openAsReader(any(ResourceLocation.class))).thenAnswer(call -> {
            ResourceLocation id = call.getArgument(0);
            return Files.newBufferedReader(assets.resolve(id.getNamespace()).resolve(id.getPath()));
        });

        TTMesh mesh = TTMeshLoader.load(resources, MESH_ID);

        assertEquals(
                List.of("glass", "brass", "ink"),
                mesh.parts().stream().map(TTMeshPart::name).toList());
        assertEquals(
                List.of(32, 48, 16),
                mesh.parts().stream().map(TTMeshPart::quadCount).toList());
        assertEquals(96, mesh.parts().stream().mapToInt(TTMeshPart::quadCount).sum());
        assertEquals(
                List.of("", "", ""),
                mesh.parts().stream().map(TTMeshPart::materialSlot).toList());

        BufferedImage texture = ImageIO.read(assets.resolve(TEXTURE_ID.getNamespace())
                .resolve(TEXTURE_ID.getPath())
                .toFile());
        assertEquals(64, texture.getWidth());
        assertEquals(32, texture.getHeight());
        texture.flush();
    }

    @Test
    void rendererGatesMeshDrawOnScribeToolsAndUsesSourceTexture() throws Exception {
        TTMesh mesh = loadMesh();
        MultiBufferSource buffers = mock(MultiBufferSource.class);
        VertexConsumer buffer = mock(VertexConsumer.class);
        when(buffers.getBuffer(any(RenderType.class))).thenReturn(buffer);

        try (MockedStatic<GolemMeshes> meshes = mockStatic(GolemMeshes.class)) {
            meshes.when(() -> GolemMeshes.get(MESH_ID)).thenReturn(mesh);

            ResearchTableRenderer.renderInkwell(false, new PoseStack(), buffers, LIGHT);
            meshes.verifyNoInteractions();
            verifyNoInteractions(buffers);

            ResearchTableRenderer.renderInkwell(true, new PoseStack(), buffers, LIGHT);

            meshes.verify(() -> GolemMeshes.get(MESH_ID));
            meshes.verify(
                    () -> GolemMeshes.renderPart(
                            any(TTMeshPart.class), any(PoseStack.Pose.class), same(buffer), eq(LIGHT), eq(WHITE)),
                    times(3));
            verify(buffers).getBuffer(RenderType.entityCutout(TEXTURE_ID));
        }
    }

    @Test
    void existingFacingTransformMatchesSourceForEveryHorizontalDirection() {
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

    private static TTMesh loadMesh() throws Exception {
        Path assets = Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets");
        ResourceManager resources = mock(ResourceManager.class);
        when(resources.openAsReader(any(ResourceLocation.class))).thenAnswer(call -> {
            ResourceLocation id = call.getArgument(0);
            return Files.newBufferedReader(assets.resolve(id.getNamespace()).resolve(id.getPath()));
        });
        return TTMeshLoader.load(resources, MESH_ID);
    }
}
