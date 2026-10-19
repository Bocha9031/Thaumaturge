package com.leclowndu93150.thaumaturge.compat.jade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

class JadeAspectLabelTest {
    @Test
    void nodeAspectLabelColorsOnlyTheAspectNameAndKeepsAmount() {
        int aspectColor = 0x35A7D6;
        IAspect aspect = mock(IAspect.class);
        when(aspect.tag()).thenReturn("test_aspect");
        when(aspect.color()).thenReturn(aspectColor);

        var label = JadeComponents.aspectLabel(new AspectInstance(Holder.direct(aspect), 17));

        assertNull(label.getStyle().getColor());
        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, label.getContents());
        assertEquals("jade.thaumaturge.aspect_amount", contents.getKey());
        Component name = assertInstanceOf(Component.class, contents.getArgs()[0]);
        assertEquals(aspectColor, name.getStyle().getColor().getValue());
        assertEquals(17, contents.getArgs()[1]);
    }
}
