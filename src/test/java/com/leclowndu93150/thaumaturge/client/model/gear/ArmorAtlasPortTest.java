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

class ArmorAtlasPortTest {
    @Test
    void knightGeometryUsesAuthoredPartsAnd256By128Atlas() {
        ModelPart headRoot = KnightArmorModel.createHead().bakeRoot();
        assertNotNull(new KnightArmorModel(headRoot));
        ModelPart helmet = headRoot;
        assertNotNull(helmet.getChild("head").getChild("helmet").getChild("faceplate"));

        ModelPart chest = KnightArmorModel.createChest().bakeRoot();
        assertNotNull(new KnightArmorModel(chest));
        ModelPart armor = chest.getChild("body").getChild("chestplate");
        assertNotNull(armor.getChild("tabard_upper").getChild("tabard_middle").getChild("tabard_lower"));
        assertNotNull(armor.getChild("cape_upper").getChild("cape_middle").getChild("cape_lower"));
        assertNotNull(chest.getChild("right_arm").getChild("right_shoulder").getChild("right_pauldron"));

        ModelPart legs = KnightArmorModel.createLegs().bakeRoot();
        assertNotNull(new KnightArmorModel(legs));
        assertNotNull(legs.getChild("right_leg").getChild("right_tasset"));
        assertNotNull(legs.getChild("body").getChild("hip"));
    }

    @Test
    void knightTextureDimensionsAndHelmetAtlasTexelMatchAuthoredUvLayout() throws Exception {
        Path resources =
                Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets/thaumaturge/textures/models/armor");
        BufferedImage humanoid =
                ImageIO.read(resources.resolve("cultist_plate_layer_1.png").toFile());
        BufferedImage leggings =
                ImageIO.read(resources.resolve("cultist_plate_layer_2.png").toFile());
        assertEquals(256, humanoid.getWidth());
        assertEquals(KnightArmorModel.TEXTURE_WIDTH, humanoid.getWidth());
        assertEquals(128, humanoid.getHeight());
        assertEquals(KnightArmorModel.TEXTURE_HEIGHT, humanoid.getHeight());
        assertEquals(256, leggings.getWidth());
        assertEquals(128, leggings.getHeight());

        // The helmet's first authored cube starts at texOffs(0, 0); this front-face texel
        // confirms that the native layer asset keeps the corresponding source atlas region.
        assertEquals(0xFF0F1011, humanoid.getRGB(16, 16));
        UvBounds helmetUv = uvBounds(helmetCube());
        assertEquals(0.0F, helmetUv.minU, 0.0001F);
        assertEquals(0.25F, helmetUv.maxU, 0.0001F);
        assertEquals(0.0F, helmetUv.minV, 0.0001F);
        assertEquals(0.25F, helmetUv.maxV, 0.0001F);
        humanoid.flush();
        leggings.flush();
    }

    private static Object helmetCube() throws Exception {
        ModelPart helmet =
                KnightArmorModel.createHead().bakeRoot().getChild("head").getChild("helmet");
        Field cubesField = ModelPart.class.getDeclaredField("cubes");
        cubesField.setAccessible(true);
        return ((List<?>) cubesField.get(helmet)).get(0);
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
