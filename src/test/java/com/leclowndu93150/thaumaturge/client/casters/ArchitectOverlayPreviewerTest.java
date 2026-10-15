package com.leclowndu93150.thaumaturge.client.casters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.items.AreaPreviewer;
import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class ArchitectOverlayPreviewerTest {
    @Test
    void modernOnlyItemIsSelectedForNativePreviewRendering() {
        Item modernItem = mock(Item.class, withSettings().extraInterfaces(AreaPreviewer.class));
        ItemStack mainHand = mock(ItemStack.class);
        LocalPlayer player = mock(LocalPlayer.class);
        when(mainHand.getItem()).thenReturn(modernItem);
        when(player.getMainHandItem()).thenReturn(mainHand);

        assertTrue(modernItem instanceof AreaPreviewer);
        assertFalse(modernItem instanceof IArchitect);
        assertSame(mainHand, ArchitectOverlayRenderer.heldArchitect(player));
    }
}
