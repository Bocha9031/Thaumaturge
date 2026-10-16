package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityInhabitedZombie;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class ZombiePlateTextureParityTest {
    private static final String[] TEXTURES = {
        "/assets/thaumaturge/textures/entity/equipment/humanoid/zombie_plate.png",
        "/assets/thaumaturge/textures/entity/equipment/humanoid_leggings/zombie_plate.png",
        "/assets/thaumaturge/textures/models/armor/zombie_plate_layer_1.png",
        "/assets/thaumaturge/textures/models/armor/zombie_plate_layer_2.png"
    };
    private static final String SOURCE_SHA_256 = "3b9f8a74e07dc4f714ffac71993c29abdf6923544f0a0b1c87fb297f1a5da4f5";

    @Test
    void allArmorAndEntityTexturesUseThePinnedCurrentSourceAtlas() throws Exception {
        byte[][] textures = new byte[TEXTURES.length][];
        for (int index = 0; index < TEXTURES.length; index++) {
            textures[index] = readResource(TEXTURES[index]);
            assertEquals(SOURCE_SHA_256, sha256(textures[index]), TEXTURES[index]);
            BufferedImage decoded = image(textures[index]);
            assertEquals(256, decoded.getWidth(), TEXTURES[index]);
            assertEquals(128, decoded.getHeight(), TEXTURES[index]);
            if (index > 0) {
                assertArrayEquals(textures[0], textures[index], TEXTURES[index]);
            }
        }
    }

    @Test
    void inhabitedZombiesSelectTheMatchingPlateLayersOnly() {
        CultistPlateItem leggings = assertInstanceOf(CultistPlateItem.class, TTItems.CRIMSON_PLATE_LEGS.get());
        EntityInhabitedZombie zombie = mock(EntityInhabitedZombie.class);
        Entity ordinaryEntity = mock(Entity.class);

        assertEquals(
                TTIds.rl("textures/models/armor/zombie_plate_layer_1.png"),
                leggings.getArmorTexture(ItemStack.EMPTY, zombie, EquipmentSlot.CHEST, null, false));
        assertEquals(
                TTIds.rl("textures/models/armor/zombie_plate_layer_2.png"),
                leggings.getArmorTexture(ItemStack.EMPTY, zombie, EquipmentSlot.LEGS, null, true));
        assertNull(leggings.getArmorTexture(ItemStack.EMPTY, ordinaryEntity, EquipmentSlot.LEGS, null, true));
    }

    private static byte[] readResource(String path) throws IOException {
        try (InputStream input = ZombiePlateTextureParityTest.class.getResourceAsStream(path)) {
            assertNotNull(input, "Expected armor texture resource " + path);
            return input.readAllBytes();
        }
    }

    private static BufferedImage image(byte[] bytes) throws IOException {
        BufferedImage image = javax.imageio.ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(image, "Expected a readable PNG texture");
        return image;
    }

    private static String sha256(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        return HexFormat.of().formatHex(digest);
    }
}
