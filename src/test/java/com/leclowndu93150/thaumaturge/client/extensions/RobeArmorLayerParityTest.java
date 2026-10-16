package com.leclowndu93150.thaumaturge.client.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RobeArmorLayerParityTest {
    @Test
    void standardRobeKeepsTheTintedBaseAndPlainOverlay() {
        List<ArmorMaterial.Layer> layers = TTMaterials.ARMOR_ROBES.value().layers();

        assertEquals(2, layers.size());
        assertEquals(
                TTIds.rl("textures/models/armor/robes_layer_1.png"),
                layers.get(0).texture(false));
        assertTrue(layers.get(0).dyeable());
        assertEquals(
                TTIds.rl("textures/models/armor/robes_layer_1_over.png"),
                layers.get(1).texture(false));
        assertFalse(layers.get(1).dyeable());
    }

    @Test
    void voidRobeKeepsTheTintedOverlayAndPlainBaseInSourceOrder() {
        List<ArmorMaterial.Layer> layers = TTMaterials.ARMOR_VOID_ROBE.value().layers();

        assertEquals(2, layers.size());
        assertEquals(
                TTIds.rl("textures/models/armor/void_robe_layer_1_over.png"),
                layers.get(0).texture(false));
        assertTrue(layers.get(0).dyeable());
        assertEquals(
                TTIds.rl("textures/models/armor/void_robe_layer_2_over.png"),
                layers.get(0).texture(true));
        assertEquals(
                TTIds.rl("textures/models/armor/void_robe_layer_1.png"),
                layers.get(1).texture(false));
        assertFalse(layers.get(1).dyeable());
    }

    @Test
    void registeredRobeExtensionsUseTheSourceUndyedFallbackColor() {
        RegisterClientExtensionsEvent event = mock(RegisterClientExtensionsEvent.class);
        CultistArmorClientExtensions.onRegisterClientExtensions(event);

        ArgumentCaptor<IClientItemExtensions> extensionCaptor = ArgumentCaptor.forClass(IClientItemExtensions.class);
        ArgumentCaptor<Item[]> itemsCaptor = ArgumentCaptor.forClass(Item[].class);
        verify(event, atLeastOnce()).registerItem(extensionCaptor.capture(), itemsCaptor.capture());

        assertRegisteredFallback(extensionCaptor, itemsCaptor, TTItems.CLOTH_CHEST.get());
        assertRegisteredFallback(extensionCaptor, itemsCaptor, TTItems.CLOTH_LEGS.get());
        assertRegisteredFallback(extensionCaptor, itemsCaptor, TTItems.CLOTH_BOOTS.get());
        assertRegisteredFallback(extensionCaptor, itemsCaptor, TTItems.VOID_ROBE_HELM.get());
        assertRegisteredFallback(extensionCaptor, itemsCaptor, TTItems.VOID_ROBE_CHEST.get());
        assertRegisteredFallback(extensionCaptor, itemsCaptor, TTItems.VOID_ROBE_LEGS.get());
    }

    private static void assertRegisteredFallback(
            ArgumentCaptor<IClientItemExtensions> extensionCaptor,
            ArgumentCaptor<Item[]> itemsCaptor,
            Item expectedItem) {
        List<IClientItemExtensions> extensions = extensionCaptor.getAllValues();
        List<Item[]> itemSets = itemsCaptor.getAllValues();
        for (int index = 0; index < extensions.size(); index++) {
            for (Item registeredItem : itemSets.get(index)) {
                if (registeredItem == expectedItem) {
                    assertEquals(0xFF6A3880, extensions.get(index).getDefaultDyeColor(ItemStack.EMPTY));
                    return;
                }
            }
        }
        throw new AssertionError("Expected registration with the source robe fallback color: " + expectedItem);
    }
}
