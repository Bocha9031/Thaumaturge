package com.leclowndu93150.thaumaturge.client.model.gear;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.junit.jupiter.api.Test;

class ClericArmorAtlasPortTest {
    @Test
    void allClericArmorLayersBakeWithTheirAuthoredPartChains() {
        ModelPart head = ClericArmorModel.createHead().bakeRoot();
        assertNotNull(new ClericArmorModel(head));
        assertNotNull(head.getChild("head").getChild("hood").getChild("hood_r1"));
        assertNotNull(head.getChild("head").getChild("hood").getChild("hood_r2"));

        ModelPart chest = ClericArmorModel.createChest().bakeRoot();
        assertNotNull(new ClericArmorModel(chest));
        ModelPart robe = chest.getChild("body").getChild("robe");
        assertNotNull(robe.getChild("front_flap_upper").getChild("front_flap_lower"));
        assertNotNull(robe.getChild("back_flap_upper").getChild("back_flap_lower"));
        assertNotNull(chest.getChild("right_arm").getChild("right_sleeve").getChild("right_mantle"));
        assertNotNull(chest.getChild("left_arm").getChild("left_sleeve").getChild("left_mantle"));

        ModelPart legs = ClericArmorModel.createLegs().bakeRoot();
        assertNotNull(new ClericArmorModel(legs));
        assertNotNull(legs.getChild("right_leg").getChild("right_skirt").getChild("right_skirt_panel"));
        assertNotNull(legs.getChild("left_leg").getChild("left_skirt").getChild("left_skirt_panel"));
        assertNotNull(legs.getChild("body").getChild("hip"));
    }

    @Test
    void clericAtlasAndBakedHoodUvMatchTheSourceLayout() throws Exception {
        Path resources =
                Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets/thaumaturge/textures/models/armor");
        Path chestPath = resources.resolve("cultist_robe_layer_1.png");
        Path legsPath = resources.resolve("cultist_robe_layer_2.png");
        BufferedImage chest = ImageIO.read(chestPath.toFile());
        BufferedImage legs = ImageIO.read(legsPath.toFile());
        assertEquals(128, chest.getWidth());
        assertEquals(128, chest.getHeight());
        assertEquals(128, legs.getWidth());
        assertEquals(128, legs.getHeight());
        assertArrayEquals(Files.readAllBytes(chestPath), Files.readAllBytes(legsPath));

        ModelPart hood =
                ClericArmorModel.createHead().bakeRoot().getChild("head").getChild("hood");
        UvBounds uv = uvBounds(firstCube(hood));
        assertEquals(5.0F / 128.0F, uv.minU, 0.0001F);
        assertEquals(41.0F / 128.0F, uv.maxU, 0.0001F);
        assertEquals(47.0F / 128.0F, uv.minV, 0.0001F);
        assertEquals(65.0F / 128.0F, uv.maxV, 0.0001F);
        chest.flush();
        legs.flush();
    }

    @Test
    void clothAnimationResetsAcrossCallsAndPreservesCrouchSeatAndArmorStandBehavior() {
        ModelPart root = ClericArmorModel.createChest().bakeRoot();
        ClericArmorModel model = new ClericArmorModel(root);
        ModelPart robe = root.getChild("body").getChild("robe");
        ModelPart front = robe.getChild("front_flap_upper");
        ModelPart back = robe.getChild("back_flap_upper");
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.isCrouching()).thenReturn(false);
        when(entity.isPassenger()).thenReturn(false);

        model.setupAnim(entity, 0.5F, 0.6F, 10.0F, 0.0F, 0.0F);
        float frontRot = front.xRot;
        float frontHemRot = front.getChild("front_flap_lower").xRot;
        float backRot = back.xRot;
        float backHemRot = back.getChild("back_flap_lower").xRot;
        model.setupAnim(entity, 0.5F, 0.6F, 10.0F, 0.0F, 0.0F);
        assertEquals(frontRot, front.xRot, 0.0001F);
        assertEquals(frontHemRot, front.getChild("front_flap_lower").xRot, 0.0001F);
        assertEquals(backRot, back.xRot, 0.0001F);
        assertEquals(backHemRot, back.getChild("back_flap_lower").xRot, 0.0001F);

        when(entity.isCrouching()).thenReturn(true);
        when(entity.isPassenger()).thenReturn(true);
        model.setupAnim(entity, 0.5F, 0.6F, 10.0F, 0.0F, 0.0F);
        assertEquals(2.5F, front.y, 0.0001F);
        assertEquals(0.4F, front.xScale, 0.0001F);

        ArmorStand stand = mock(ArmorStand.class);
        Rotations pose = new Rotations(0.0F, 0.0F, 0.0F);
        when(stand.getHeadPose()).thenReturn(pose);
        when(stand.getBodyPose()).thenReturn(pose);
        when(stand.getLeftArmPose()).thenReturn(pose);
        when(stand.getRightArmPose()).thenReturn(pose);
        when(stand.getLeftLegPose()).thenReturn(pose);
        when(stand.getRightLegPose()).thenReturn(pose);
        model.setupAnim(stand, 0.5F, 0.6F, 10.0F, 0.0F, 0.0F);
        assertEquals(-0.0873F, front.xRot, 0.0001F);
        assertEquals(-0.0873F, back.xRot, 0.0001F);
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
