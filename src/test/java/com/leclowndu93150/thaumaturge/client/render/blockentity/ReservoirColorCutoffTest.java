package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Holder;
import org.junit.jupiter.api.Test;

class ReservoirColorCutoffTest {
    private static Holder<IAspect> aspect(int color) {
        IAspect aspect = mock(IAspect.class);
        when(aspect.color()).thenReturn(color);
        return Holder.direct(aspect);
    }

    @Test
    void colorCyclesFromPreviousAspectUsingCutoffSrgbInterpolationAndPartialTicks() {
        AspectList contents = AspectList.EMPTY.add(aspect(0xFF0000), 1).add(aspect(0x0000FF), 1);
        assertEquals(0xE60000FF, EssentiaReservoirRenderer.fluidColor(contents, 0, 0));
        assertEquals(0xE67F007F, EssentiaReservoirRenderer.fluidColor(contents, 10, 0));
        assertEquals(0xE6FF0000, EssentiaReservoirRenderer.fluidColor(contents, 20, 0));
        assertEquals(0xE60000FF, EssentiaReservoirRenderer.fluidColor(contents, 40, 0));
        assertNotEquals(
                EssentiaReservoirRenderer.fluidColor(contents, 9, 0),
                EssentiaReservoirRenderer.fluidColor(contents, 9, 0.5F));
    }

    @Test
    void emptyAndSingleAspectColorsNeedNoClientTickState() {
        assertEquals(-1, EssentiaReservoirRenderer.fluidColor(AspectList.EMPTY, 99, 0.5F));
        AspectList contents = AspectList.EMPTY.add(aspect(0x123456), 1);
        assertEquals(0xE6123456, EssentiaReservoirRenderer.fluidColor(contents, 0, 0));
        assertEquals(0xE6123456, EssentiaReservoirRenderer.fluidColor(contents, 123, 0.75F));
    }

    @Test
    void blockLightFloorPreservesSkyLightAndAlreadyBrighterBlockLight() {
        assertEquals(LightTexture.pack(12, 10), EssentiaReservoirRenderer.fluidLight(LightTexture.pack(3, 10)));
        assertEquals(LightTexture.pack(15, 2), EssentiaReservoirRenderer.fluidLight(LightTexture.pack(15, 2)));
    }
}
