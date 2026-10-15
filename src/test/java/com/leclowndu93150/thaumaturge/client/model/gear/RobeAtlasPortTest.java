package com.leclowndu93150.thaumaturge.client.model.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.Test;

class RobeAtlasPortTest {
    @Test
    void allArmorLayerRootsBakeWithCompleteSharedTailChains() {
        ModelPart head = RobeArmorModel.createHead().bakeRoot();
        assertNotNull(new RobeArmorModel(head));
        assertNotNull(head.getChild("head").getChild("hood_peak_upper"));

        ModelPart chest = RobeArmorModel.createChest().bakeRoot();
        assertNotNull(new RobeArmorModel(chest));
        ModelPart body = chest.getChild("body");
        assertNotNull(body.getChild("tail_right_upper").getChild("tail_right_lower"));
        assertNotNull(body.getChild("tail_left_upper").getChild("tail_left_lower"));
        assertEquals(-5.75F, body.getChild("void_tome").x);
        assertEquals(12.6F, body.getChild("void_tome").y);
        assertEquals(0.25F, body.getChild("void_tome").z);
        assertEquals(1.5707964F, body.getChild("void_tome").xRot, 0.0001F);
        assertEquals(1.3507963F, body.getChild("void_tome").yRot, 0.0001F);
        assertEquals(1.6907964F, body.getChild("void_tome").zRot, 0.0001F);

        ModelPart legs = RobeArmorModel.createLegs().bakeRoot();
        assertNotNull(new RobeArmorModel(legs));
    }

    @Test
    void sourceLayerImagesAndBakedRobeUvMatchTheirAtlasDimensions() throws Exception {
        Path resources =
                Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets/thaumaturge/textures/models/armor");
        BufferedImage cultist =
                ImageIO.read(resources.resolve("cultist_robe_layer_1.png").toFile());
        BufferedImage cultistLeggings =
                ImageIO.read(resources.resolve("cultist_robe_layer_2.png").toFile());
        BufferedImage voidRobe =
                ImageIO.read(resources.resolve("void_robe_layer_1.png").toFile());
        BufferedImage voidRobeLeggings =
                ImageIO.read(resources.resolve("void_robe_layer_2.png").toFile());
        BufferedImage voidOverlay =
                ImageIO.read(resources.resolve("void_robe_layer_1_over.png").toFile());
        BufferedImage voidOverlayLeggings =
                ImageIO.read(resources.resolve("void_robe_layer_2_over.png").toFile());
        assertEquals(128, cultist.getWidth());
        assertEquals(128, cultist.getHeight());
        assertEquals(128, cultistLeggings.getWidth());
        assertEquals(128, cultistLeggings.getHeight());
        assertEquals(256, voidRobe.getWidth());
        assertEquals(256, voidRobe.getHeight());
        assertEquals(256, voidRobeLeggings.getWidth());
        assertEquals(256, voidRobeLeggings.getHeight());
        assertEquals(256, voidOverlay.getWidth());
        assertEquals(256, voidOverlay.getHeight());
        assertEquals(256, voidOverlayLeggings.getWidth());
        assertEquals(256, voidOverlayLeggings.getHeight());
        assertEquals(0xFF1D1E20, cultist.getRGB(58, 0));
        assertEquals(0xFF080611, voidRobe.getRGB(22, 40));
        assertEquals(0xFF989EAE, voidOverlay.getRGB(20, 2));

        ModelPart head = RobeArmorModel.createHead().bakeRoot().getChild("head");
        UvBounds uv = uvBounds(cubeAt(head, 0));
        assertEquals(1.0F / 128.0F, uv.minU, 0.0001F);
        assertEquals(38.0F / 128.0F, uv.maxU, 0.0001F);
        assertEquals(1.0F / 128.0F, uv.minV, 0.0001F);
        assertEquals(19.0F / 128.0F, uv.maxV, 0.0001F);
        assertEquals(5, polygonCount(cubeAt(head, 2)));
        cultist.flush();
        cultistLeggings.flush();
        voidRobe.flush();
        voidRobeLeggings.flush();
        voidOverlay.flush();
        voidOverlayLeggings.flush();
    }

    @Test
    void tailAnimationMatchesSourceInputsAndResetsBetweenPoses() {
        ModelPart root = RobeArmorModel.createChest().bakeRoot();
        RobeArmorModel model = new RobeArmorModel(root);
        ModelPart body = root.getChild("body");
        ModelPart rightTail = body.getChild("tail_right_upper");
        ModelPart rightHem = rightTail.getChild("tail_right_lower");
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.isCrouching()).thenReturn(false);
        when(entity.isFallFlying()).thenReturn(false);
        when(entity.isVisuallySwimming()).thenReturn(false);

        float limbSwing = 0.8F;
        float speed = 0.75F;
        float age = 12.0F;
        model.setupAnim(entity, limbSwing, speed, age, 0.0F, 0.0F);
        assertSourcePose(model, body, rightTail, rightHem, limbSwing, speed, age, false, false, false);
        float firstTailPitch = rightTail.xRot;
        float firstHemPitch = rightHem.xRot;

        model.setupAnim(entity, limbSwing, speed, age, 0.0F, 0.0F);
        assertEquals(firstTailPitch, rightTail.xRot, 0.0001F);
        assertEquals(firstHemPitch, rightHem.xRot, 0.0001F);

        when(entity.isCrouching()).thenReturn(true);
        when(entity.isFallFlying()).thenReturn(true);
        model.setupAnim(entity, limbSwing, speed, age, 0.0F, 0.0F);
        assertSourcePose(model, body, rightTail, rightHem, limbSwing, speed, age, true, true, false);

        when(entity.isCrouching()).thenReturn(false);
        when(entity.isFallFlying()).thenReturn(false);
        when(entity.isVisuallySwimming()).thenReturn(true);
        model.setupAnim(entity, limbSwing, speed, age, 0.0F, 0.0F);
        assertSourcePose(model, body, rightTail, rightHem, limbSwing, speed, age, false, false, true);
    }

    private static void assertSourcePose(
            RobeArmorModel model,
            ModelPart body,
            ModelPart tail,
            ModelPart hem,
            float limbSwing,
            float speed,
            float age,
            boolean crouching,
            boolean fallFlying,
            boolean swimming) {
        float clampedSpeed = Mth.clamp(speed, 0.0F, 1.0F);
        float stride = Math.abs(Mth.cos(limbSwing * 0.6662F)) * clampedSpeed;
        float flutter = Mth.sin(age * 0.16F) * (0.012F + clampedSpeed * 0.028F);
        float trailing = fallFlying || swimming ? 0.65F : 0.0F;
        float backLeg = Math.max(0.0F, model.rightLeg.xRot - body.xRot);
        float expectedTail = 6.0F * (float) (Math.PI / 180.0)
                + backLeg * 1.25F
                + stride * 0.1F
                + flutter
                + (crouching ? 0.5F : 0.0F)
                + trailing;
        float expectedHem = 4.0F * (float) (Math.PI / 180.0) + stride * 0.12F + flutter * 1.5F;
        assertEquals(expectedTail, tail.xRot, 0.0001F);
        assertEquals(expectedHem, hem.xRot, 0.0001F);
    }

    private static Object cubeAt(ModelPart part, int index) throws Exception {
        Field cubesField = ModelPart.class.getDeclaredField("cubes");
        cubesField.setAccessible(true);
        return ((List<?>) cubesField.get(part)).get(index);
    }

    private static int polygonCount(Object cube) throws Exception {
        Field polygonsField = cube.getClass().getDeclaredField("polygons");
        polygonsField.setAccessible(true);
        return Array.getLength(polygonsField.get(cube));
    }

    private static UvBounds uvBounds(Object cube) throws Exception {
        Field polygonsField = cube.getClass().getDeclaredField("polygons");
        polygonsField.setAccessible(true);
        Object polygons = polygonsField.get(cube);
        float minU = Float.POSITIVE_INFINITY;
        float maxU = Float.NEGATIVE_INFINITY;
        float minV = Float.POSITIVE_INFINITY;
        float maxV = Float.NEGATIVE_INFINITY;
        for (int polygonIndex = 0; polygonIndex < Array.getLength(polygons); polygonIndex++) {
            Object polygon = Array.get(polygons, polygonIndex);
            Field verticesField = polygon.getClass().getDeclaredField("vertices");
            verticesField.setAccessible(true);
            Object vertices = verticesField.get(polygon);
            for (int vertexIndex = 0; vertexIndex < Array.getLength(vertices); vertexIndex++) {
                Object vertex = Array.get(vertices, vertexIndex);
                Field uField = vertex.getClass().getDeclaredField("u");
                Field vField = vertex.getClass().getDeclaredField("v");
                uField.setAccessible(true);
                vField.setAccessible(true);
                float u = uField.getFloat(vertex);
                float v = vField.getFloat(vertex);
                minU = Math.min(minU, u);
                maxU = Math.max(maxU, u);
                minV = Math.min(minV, v);
                maxV = Math.max(maxV, v);
            }
        }
        return new UvBounds(minU, maxU, minV, maxV);
    }

    private record UvBounds(float minU, float maxU, float minV, float maxV) {}
}
