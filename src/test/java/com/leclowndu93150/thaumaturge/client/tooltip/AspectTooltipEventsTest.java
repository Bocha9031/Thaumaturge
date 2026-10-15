package com.leclowndu93150.thaumaturge.client.tooltip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AspectTooltipEventsTest {
    @Test
    void aspectVisibilityFollowsShiftAndDefaultSetting() {
        assertFalse(AspectTooltipEvents.shouldShowAspects(false, false));
        assertTrue(AspectTooltipEvents.shouldShowAspects(true, false));
        assertTrue(AspectTooltipEvents.shouldShowAspects(false, true));
        assertFalse(AspectTooltipEvents.shouldShowAspects(true, true));
    }

    @Test
    void tooltipCallbackAddsChipsOnlyWhenShiftDiffersFromDefaultSetting() {
        ItemStack stack = new ItemStack(Items.STONE);
        IAspectIndex index = mock(IAspectIndex.class);
        AspectList aspects = mock(AspectList.class);
        when(aspects.isEmpty()).thenReturn(false);
        when(index.of(stack)).thenReturn(aspects);

        try (MockedStatic<Screen> screen = mockStatic(Screen.class);
                MockedStatic<ThaumaturgeClientConfig> config = mockStatic(ThaumaturgeClientConfig.class);
                MockedStatic<AspectIndexAccess> access = mockStatic(AspectIndexAccess.class)) {
            access.when(AspectIndexAccess::index).thenReturn(index);

            for (boolean showByDefault : List.of(false, true)) {
                config.when(ThaumaturgeClientConfig::showAspectsByDefault).thenReturn(showByDefault);
                for (boolean shift : List.of(false, true)) {
                    screen.when(Screen::hasShiftDown).thenReturn(shift);
                    List<Either<FormattedText, TooltipComponent>> elements = new ArrayList<>();
                    RenderTooltipEvent.GatherComponents event =
                            new RenderTooltipEvent.GatherComponents(stack, 320, 240, elements, 0);

                    AspectTooltipEvents.onGatherTooltipComponents(event);

                    assertEquals(shift != showByDefault, elements.size() == 1);
                }
            }
        }
    }
}
