package com.leclowndu93150.thaumaturge.client.taint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class TaintTraitVisualsTest {
    @Test
    void fluxSwirlUsesBalancedTintAndGrowsByItsScale() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.25F, 0.5F);

        FluxSwirlParticleOptions particle = TaintTraitVisuals.createFluxSwirl(random);

        assertEquals(76, ARGB32.alpha(particle.color()));
        assertEquals(47, ARGB32.red(particle.color()));
        assertEquals(7, ARGB32.green(particle.color()));
        assertEquals(51, ARGB32.blue(particle.color()));
        assertEquals(5.0F, particle.scale());
        assertEquals(6.25F, particle.endScale());
    }
}
