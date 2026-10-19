package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

class ElementalHoeItemTest {
    @Test
    void secondaryUseSelectsTheVanillaHoeAction() {
        Player player = mock(Player.class);
        when(player.isSecondaryUseActive()).thenReturn(true);

        assertTrue(ElementalHoeItem.usesVanillaHoe(player));
    }

    @Test
    void ordinaryUseKeepsTheAreaAction() {
        Player player = mock(Player.class);
        when(player.isSecondaryUseActive()).thenReturn(false);

        assertFalse(ElementalHoeItem.usesVanillaHoe(player));
    }
}
