package com.leclowndu93150.thaumaturge.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AuraHudOverlayTest {
    @Test
    void auraSharesAreClampedToTheUpdatedTubeCapacity() {
        assertEquals(0.0F, AuraHudOverlay.share(-1.0F));
        assertEquals(0.5F, AuraHudOverlay.share(260.0F));
        assertEquals(1.0F, AuraHudOverlay.share(520.0F));
        assertEquals(1.0F, AuraHudOverlay.share(900.0F));
    }

    @Test
    void shiftedHudAmountsUseOneDecimalWithoutGrouping() {
        assertEquals("1234.6", AuraHudOverlay.amount(1234.56F));
        assertEquals("20", AuraHudOverlay.amount(20.0F));
        assertEquals("0", AuraHudOverlay.amount(0.04F));
    }
}
