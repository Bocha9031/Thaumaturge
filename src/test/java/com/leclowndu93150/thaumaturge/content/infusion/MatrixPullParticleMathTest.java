package com.leclowndu93150.thaumaturge.content.infusion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class MatrixPullParticleMathTest {
    @Test
    void usesMainParticleCountAndKick() {
        assertEquals(4, MatrixPullParticleMath.SOURCE_PARTICLE_COUNT);
        assertEquals(0.03, MatrixPullParticleMath.CRUMB_ITEM_KICK, 0.0);
    }

    @Test
    void nonBlockItemStartsStayInsideTheBoundedPedestalBox() {
        BlockPos source = new BlockPos(-12, 40, 7);
        Vec3 lower = MatrixPullParticleMath.nonBlockItemStart(source, 0.0, 0.0, 0.0);
        Vec3 upper = MatrixPullParticleMath.nonBlockItemStart(source, 0.999999, 0.999999, 0.999999);

        assertEquals(-11.6, lower.x, 1.0e-9);
        assertEquals(41.23, lower.y, 1.0e-9);
        assertEquals(7.4, lower.z, 1.0e-9);
        assertTrue(upper.x < -11.4 && upper.x > lower.x);
        assertTrue(upper.y < 41.43 && upper.y > lower.y);
        assertTrue(upper.z < 7.6 && upper.z > lower.z);
    }

    @Test
    void sparkleColorsUseTheMainRanges() {
        assertEquals(0.4F, MatrixPullParticleMath.sparkleRed(0.0F), 0.0F);
        assertEquals(0.6F, MatrixPullParticleMath.sparkleRed(1.0F), 0.0F);
        assertEquals(0.6F, MatrixPullParticleMath.sparkleBlue(0.0F), 0.0F);
        assertEquals(0.9F, MatrixPullParticleMath.sparkleBlue(1.0F), 1.0e-6F);
        assertEquals(0.2F, MatrixPullParticleMath.SPARKLE_GREEN, 0.0F);
    }
}
