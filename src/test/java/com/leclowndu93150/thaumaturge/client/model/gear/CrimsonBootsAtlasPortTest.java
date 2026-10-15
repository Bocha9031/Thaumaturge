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

class CrimsonBootsAtlasPortTest {
    @Test
    void bootsLayerBakesBothLegsAndUsesTheNative64By32Atlas() throws Exception {
        ModelPart root = CrimsonBootsModel.createFeet().bakeRoot();
        assertNotNull(new CrimsonBootsModel(root));
        ModelPart rightLeg = root.getChild("right_leg");
        ModelPart leftLeg = root.getChild("left_leg");
        assertEquals(-1.9F, rightLeg.x, 0.0001F);
        assertEquals(1.9F, leftLeg.x, 0.0001F);

        Path texture = Path.of(
                System.getProperty("thaumaturge.testResourceRoot"),
                "assets/thaumaturge/textures/models/armor/cultist_boots_layer_1.png");
        BufferedImage atlas = ImageIO.read(texture.toFile());
        assertEquals(64, atlas.getWidth());
        assertEquals(32, atlas.getHeight());

        UvBounds uv = uvBounds(firstCube(rightLeg));
        assertEquals(0.0F, uv.minU, 0.0001F);
        assertEquals(0.25F, uv.maxU, 0.0001F);
        assertEquals(0.5F, uv.minV, 0.0001F);
        assertEquals(1.0F, uv.maxV, 0.0001F);
        atlas.flush();
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
