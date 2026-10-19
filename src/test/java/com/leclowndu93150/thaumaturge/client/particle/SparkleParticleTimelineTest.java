package com.leclowndu93150.thaumaturge.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

final class SparkleParticleTimelineTest {
    @Test
    void lifetimeClampsNonPositiveBaseAge() {
        assertEquals(4, SparkleParticle.lifetimeFor(0, RandomSource.create(1L)));
        assertEquals(4, SparkleParticle.lifetimeFor(-5, RandomSource.create(1L)));
    }

    @Test
    void flickerCurveInterpolatesBetweenKeysAndFadesAtLifetime() {
        RandomSource random = RandomSource.create(7L);
        float[] keys = SparkleParticle.buildFlickerKeys(40, random);

        assertEquals(12, keys.length);
        assertEquals(1.0F, SparkleParticle.flickerAlpha(0, keys));
        assertEquals(Mth.lerp(0.25F, keys[0], keys[1]), SparkleParticle.flickerAlpha(1, keys));
        assertEquals(0.0F, SparkleParticle.flickerAlpha(40, keys));
        assertTrue(keys[10] == 0.0F && keys[11] == 0.0F);
    }
}
