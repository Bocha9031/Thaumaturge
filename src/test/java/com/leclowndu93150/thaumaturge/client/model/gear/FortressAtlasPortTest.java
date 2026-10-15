package com.leclowndu93150.thaumaturge.client.model.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

class FortressAtlasPortTest {
    @Test
    void allFortressLayerRootsBakeWithTheirAuthoredPartsAndPivots() {
        ModelPart headRoot = FortressArmorModel.createHead().bakeRoot();
        assertNotNull(new FortressArmorModel(headRoot));
        ModelPart head = headRoot.getChild("head");
        assertNotNull(head.getChild("swept_crest_-1"));
        assertNotNull(head.getChild("swept_crest_1"));
        for (int mask = 0; mask < 3; mask++) {
            assertFalse(head.getChild("mask_" + mask).visible);
        }
        assertFalse(head.getChild("goggles").visible);

        ModelPart chestRoot = FortressArmorModel.createChest().bakeRoot();
        assertNotNull(new FortressArmorModel(chestRoot));
        assertEquals(-5.0F, chestRoot.getChild("right_arm").x);
        assertEquals(5.0F, chestRoot.getChild("left_arm").x);
        assertEquals(2.0F, chestRoot.getChild("right_arm").y);
        assertEquals(2.0F, chestRoot.getChild("left_arm").y);

        ModelPart legsRoot = FortressArmorModel.createLegs().bakeRoot();
        assertNotNull(new FortressArmorModel(legsRoot));
        assertEquals(-1.9F, legsRoot.getChild("right_leg").x);
        assertEquals(1.9F, legsRoot.getChild("left_leg").x);
        assertEquals(12.0F, legsRoot.getChild("right_leg").y);
        assertEquals(12.0F, legsRoot.getChild("left_leg").y);
    }

    @Test
    void fortressAtlasesAndMaskUvsMatchTheAuthored128PixelMesh() throws Exception {
        Path armorTextures =
                Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets/thaumaturge/textures/models/armor");
        BufferedImage humanoid =
                ImageIO.read(armorTextures.resolve("fortress_layer_1.png").toFile());
        BufferedImage leggings =
                ImageIO.read(armorTextures.resolve("fortress_layer_2.png").toFile());
        assertEquals(256, humanoid.getWidth());
        assertEquals(256, humanoid.getHeight());
        assertEquals(256, leggings.getWidth());
        assertEquals(256, leggings.getHeight());
        assertEquals(0xFF35170E, humanoid.getRGB(173, 2));
        assertEquals(0xFF35170E, leggings.getRGB(173, 2));

        ModelPart head = FortressArmorModel.createHead().bakeRoot().getChild("head");
        UvBounds maskUv = uvBounds(firstCube(head.getChild("mask_0")));
        assertEquals(1.5F / 128.0F, maskUv.minU, 0.0001F);
        assertEquals(20.0F / 128.0F, maskUv.maxU, 0.0001F);
        assertEquals(1.5F / 128.0F, maskUv.minV, 0.0001F);
        assertEquals(6.5F / 128.0F, maskUv.maxV, 0.0001F);

        UvBounds gogglesUv = uvBounds(firstCube(head.getChild("goggles")));
        assertEquals(61.5F / 128.0F, gogglesUv.minU, 0.0001F);
        assertEquals(80.0F / 128.0F, gogglesUv.maxU, 0.0001F);
        assertEquals(1.5F / 128.0F, gogglesUv.minV, 0.0001F);
        assertEquals(6.5F / 128.0F, gogglesUv.maxV, 0.0001F);
        humanoid.flush();
        leggings.flush();
    }

    private static Object firstCube(ModelPart part) throws Exception {
        Field cubesField = ModelPart.class.getDeclaredField("cubes");
        cubesField.setAccessible(true);
        return ((List<?>) cubesField.get(part)).get(0);
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
