package com.leclowndu93150.thaumaturge.client.render.research;

import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class EntryIconRendererTest {
    @Test
    void researchIconsUseAlphaBlendingWithTheExistingLockedTint() {
        GuiGraphics graphics = mock(GuiGraphics.class);
        try (MockedStatic<GuiBlend> blend = mockStatic(GuiBlend.class)) {
            for (String name : new String[] {"r_researcher1", "r_researcher2", "r_runicupg"}) {
                ResourceLocation texture = TTIds.rl("textures/research/" + name + ".png");
                for (boolean locked : new boolean[] {false, true}) {
                    EntryIconRenderer.drawResearchIcon(graphics, 10, 20, texture, locked);
                    int tint = locked ? 0xFF333333 : 0xFFFFFFFF;
                    blend.verify(
                            () -> GuiBlend.blitTinted(graphics, texture, 10, 20, 0.0F, 0.0F, 16, 16, 16, 16, tint));
                }
            }
            blend.verifyNoMoreInteractions();
        }
    }

    @Test
    void ordinaryTextureIconsKeepTheirAlphaBlendedRendering() {
        GuiGraphics graphics = mock(GuiGraphics.class);
        ResourceLocation texture = TTIds.rl("textures/research/r_alchemy.png");
        try (MockedStatic<GuiBlend> blend = mockStatic(GuiBlend.class)) {
            EntryIconRenderer.drawResearchIcon(graphics, 10, 20, texture, false);

            blend.verify(() -> GuiBlend.blitTinted(graphics, texture, 10, 20, 0.0F, 0.0F, 16, 16, 16, 16, 0xFFFFFFFF));
            blend.verifyNoMoreInteractions();
        }
    }
}
