package com.leclowndu93150.thaumaturge.client.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshLoader;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class GolemAssetReplacementPortTest {
    private static final Path ASSETS = Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets");

    @Test
    void materialSkinsUsePinnedBlockTextures() {
        assertEquals(
                TTIds.rl("textures/block/base_wood.png"),
                TTGolemParts.WOOD.get().texture());
        assertEquals(
                ResourceLocation.withDefaultNamespace("textures/block/iron_block.png"),
                TTGolemParts.IRON.get().texture());
        assertEquals(
                ResourceLocation.withDefaultNamespace("textures/block/terracotta.png"),
                TTGolemParts.CLAY.get().texture());
        assertEquals(
                TTIds.rl("textures/block/metal_brass.png"),
                TTGolemParts.BRASS.get().texture());
        assertEquals(
                TTIds.rl("textures/block/metal_thaumium.png"),
                TTGolemParts.THAUMIUM.get().texture());
        assertEquals(
                TTIds.rl("textures/block/metal_void.png"),
                TTGolemParts.VOID.get().texture());
    }

    @Test
    void replacedPartTexturesFallBackToTheirGolemMaterial() {
        List<GolemPart> parts = List.of(
                TTGolemParts.HEAD_SMART.get(),
                TTGolemParts.HEAD_SCOUT.get(),
                TTGolemParts.HEAD_SMART_SCOUT.get(),
                TTGolemParts.ARMS_CLAWS.get(),
                TTGolemParts.ARMS_BREAKERS.get(),
                TTGolemParts.ARMS_DARTS.get(),
                TTGolemParts.LEGS_ROLLER.get(),
                TTGolemParts.LEGS_FLYER.get(),
                TTGolemParts.ADDON_HAULER.get());

        for (GolemPart part : parts) {
            assertFalse(part.models().isEmpty());
            for (GolemPartModel model : part.models()) {
                assertNull(model.texture());
            }
        }
    }

    @Test
    void replacementMeshSlotsOverrideMaterialFallbackAndBlankSlotsUseIt() throws Exception {
        ResourceManager resources = mock(ResourceManager.class);
        when(resources.openAsReader(any(ResourceLocation.class))).thenAnswer(invocation -> {
            ResourceLocation location = invocation.getArgument(0);
            return Files.newBufferedReader(
                    ASSETS.resolve(location.getNamespace()).resolve(location.getPath()));
        });

        TTMesh mesh = TTMeshLoader.load(resources, TTIds.rl("models/mesh/golem_arms_breakers.ttmesh"));
        ResourceLocation fallback = TTGolemParts.THAUMIUM.get().texture();
        ResourceLocation iron = ResourceLocation.withDefaultNamespace("textures/block/iron_block.png");
        TTMeshPart ironSlot = mesh.parts().stream()
                .filter(part -> part.materialSlot().equals(iron.toString()))
                .findFirst()
                .orElseThrow();
        TTMeshPart fallbackSlot = mesh.parts().stream()
                .filter(part -> part.materialSlot().isEmpty())
                .findFirst()
                .orElseThrow();

        assertEquals(iron, GolemMeshes.texture(ironSlot, fallback));
        assertEquals(fallback, GolemMeshes.texture(fallbackSlot, fallback));
    }

    @Test
    void deletedGolemPngsAreAbsentFromTheResourceTree() {
        List<String> removedTextures = List.of(
                "thaumaturge/textures/entity/golems/golem_arms_breakers.png",
                "thaumaturge/textures/entity/golems/golem_arms_claws.png",
                "thaumaturge/textures/entity/golems/golem_arms_darter.png",
                "thaumaturge/textures/entity/golems/golem_hauler.png",
                "thaumaturge/textures/entity/golems/golem_head_other.png",
                "thaumaturge/textures/entity/golems/golem_legs_floater.png",
                "thaumaturge/textures/entity/golems/golem_legs_wheel.png",
                "thaumaturge/textures/entity/golems/mat_brass.png",
                "thaumaturge/textures/entity/golems/mat_clay.png",
                "thaumaturge/textures/entity/golems/mat_iron.png",
                "thaumaturge/textures/entity/golems/mat_thaumium.png",
                "thaumaturge/textures/entity/golems/mat_void.png",
                "thaumaturge/textures/entity/golems/mat_wood.png",
                "thaumaturge/textures/item/golem.png",
                "thaumaturge/textures/models/golem_decoration.png");

        for (String relativePath : removedTextures) {
            assertFalse(Files.exists(ASSETS.resolve(relativePath)), relativePath);
        }
        assertTrue(Files.exists(ASSETS.resolve("thaumaturge/textures/entity/golem/copper_golem.png")));
        assertTrue(Files.exists(ASSETS.resolve("thaumaturge/textures/entity/golem/copper_golem_eyes.png")));
    }
}
