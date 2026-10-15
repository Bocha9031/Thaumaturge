package com.leclowndu93150.thaumaturge.client.model.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

class PraetorAtlasPortTest {
    @Test
    void allPraetorLayerRootsBakeWithTheirAuthoredPartChains() {
        ModelPart head = PraetorArmorModel.createHead().bakeRoot();
        assertNotNull(new PraetorArmorModel(head));
        assertNotNull(head.getChild("head").getChild("helmet").getChild("crest"));

        ModelPart chest = PraetorArmorModel.createChest().bakeRoot();
        assertNotNull(new PraetorArmorModel(chest));
        ModelPart armor = chest.getChild("body").getChild("chestplate");
        assertNotNull(armor.getChild("right_vestment_upper")
                .getChild("right_vestment_middle")
                .getChild("right_vestment_lower"));
        assertNotNull(armor.getChild("left_vestment_upper")
                .getChild("left_vestment_middle")
                .getChild("left_vestment_lower"));
        assertNotNull(armor.getChild("tabard_upper").getChild("tabard_lower"));
        assertNotNull(armor.getChild("cape_upper").getChild("cape_middle").getChild("cape_lower"));

        ModelPart legs = PraetorArmorModel.createLegs().bakeRoot();
        assertNotNull(new PraetorArmorModel(legs));
        assertNotNull(legs.getChild("right_leg").getChild("right_tasset"));
        assertNotNull(legs.getChild("left_leg").getChild("left_tasset"));
        assertNotNull(legs.getChild("body").getChild("hip"));
    }

    @Test
    void copiedAtlasMatchesAuthoredDimensionsTexelAndBakedHelmetUv() throws Exception {
        Path resources =
                Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets/thaumaturge/textures/models/armor");
        BufferedImage humanoid =
                ImageIO.read(resources.resolve("cultist_leader_layer_1.png").toFile());
        BufferedImage leggings =
                ImageIO.read(resources.resolve("cultist_leader_layer_2.png").toFile());
        assertEquals(256, humanoid.getWidth());
        assertEquals(128, humanoid.getHeight());
        assertEquals(256, leggings.getWidth());
        assertEquals(128, leggings.getHeight());
        assertEquals(0xFF0F1011, humanoid.getRGB(18, 5));

        ModelPart helmet =
                PraetorArmorModel.createHead().bakeRoot().getChild("head").getChild("helmet");
        UvBounds uv = uvBounds(firstCube(helmet));
        assertEquals(2.0F / 256.0F, uv.minU, 0.0001F);
        assertEquals(66.0F / 256.0F, uv.maxU, 0.0001F);
        assertEquals(5.0F / 128.0F, uv.minV, 0.0001F);
        assertEquals(37.0F / 128.0F, uv.maxV, 0.0001F);
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
