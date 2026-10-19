package com.leclowndu93150.thaumaturge.content.wands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class EntityAspectOrbTest {
    @Test
    void unresolvedAspectUsesUntintedFallbackInsteadOfThrowing() {
        Level level = mock(Level.class);
        EntityAspectOrb orb = new EntityAspectOrb(TTEntities.ASPECT_ORB.get(), level);
        orb.setAspect(TTAspects.AER);

        try (MockedStatic<Aspects> aspects = Mockito.mockStatic(Aspects.class)) {
            aspects.when(() -> Aspects.resolve(level, TTAspects.AER)).thenReturn(null);

            assertEquals(0xFFFFFF, orb.getAspectColor());
            assertEquals(0xFFFFFF, orb.getAspectColor());
            aspects.verify(() -> Aspects.resolve(level, TTAspects.AER), Mockito.times(1));
        }
    }
}
