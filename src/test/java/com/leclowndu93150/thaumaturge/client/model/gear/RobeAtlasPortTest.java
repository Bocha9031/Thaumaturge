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

class RobeAtlasPortTest {
    @Test
    void allRobeLayersBakeWithTheirAuthoredPartChainsAndPropPoses() {
        ModelPart head = RobeArmorModel.createHead().bakeRoot();
        assertNotNull(new RobeArmorModel(head));
        assertNotNull(head.getChild("head").getChild("hood_peak_upper"));
        assertNotNull(head.getChild("head").getChild("hood_peak_lower"));

        ModelPart chest = RobeArmorModel.createChest().bakeRoot();
        assertNotNull(new RobeArmorModel(chest));
        ModelPart body = chest.getChild("body");
        assertNotNull(body.getChild("sash"));
        assertNotNull(body.getChild("tail_right_upper").getChild("tail_right_lower"));
        assertNotNull(body.getChild("tail_left_upper").getChild("tail_left_lower"));
        assertNotNull(chest.getChild("left_arm").getChild("pauldron_lame_left2"));

        ModelPart scroll = body.getChild("back_scroll");
        assertEquals(8.75F, scroll.y, 0.0001F);
        assertEquals(4.3F, scroll.z, 0.0001F);
        assertEquals(-0.19F, scroll.zRot, 0.0001F);
        ModelPart tome = body.getChild("void_tome");
        assertEquals(-5.75F, tome.x, 0.0001F);
        assertEquals(12.6F, tome.y, 0.0001F);
        assertEquals(0.25F, tome.z, 0.0001F);
        assertEquals(1.5707964F, tome.xRot, 0.0001F);
        assertEquals(1.3507963F, tome.yRot, 0.0001F);
        assertEquals(1.6907964F, tome.zRot, 0.0001F);

        ModelPart legs = RobeArmorModel.createLegs().bakeRoot();
        assertNotNull(new RobeArmorModel(legs));
        assertNotNull(legs.getChild("right_leg"));
        assertNotNull(legs.getChild("left_leg"));
    }

    @Test
    void robeAtlasAssetsAndBakedHoodUvKeepTheirNativeMaterialLayout() throws Exception {
        Path resources =
                Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets/thaumaturge/textures/models/armor");
        BufferedImage robes =
                ImageIO.read(resources.resolve("robes_layer_1.png").toFile());
        BufferedImage voidRobe =
                ImageIO.read(resources.resolve("void_robe_layer_1.png").toFile());
        assertEquals(64, robes.getWidth());
        assertEquals(32, robes.getHeight());
        assertEquals(256, voidRobe.getWidth());
        assertEquals(256, voidRobe.getHeight());

        ModelPart hood = RobeArmorModel.createHead().bakeRoot().getChild("head");
        UvBounds uv = uvBounds(firstCube(hood));
        assertEquals(1.0F / 128.0F, uv.minU, 0.0001F);
        assertEquals(38.0F / 128.0F, uv.maxU, 0.0001F);
        assertEquals(1.0F / 128.0F, uv.minV, 0.0001F);
        assertEquals(19.0F / 128.0F, uv.maxV, 0.0001F);
        robes.flush();
        voidRobe.flush();
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
