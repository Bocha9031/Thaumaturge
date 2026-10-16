package com.leclowndu93150.thaumaturge.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class SeekerParticleLifecyclePortTest {
    @Test
    void enteringTheTargetCellDuringBaseMovementRemovesInThatSameTick() {
        CountingSeeker particle = new CountingSeeker(mock(ClientLevel.class), 0.9, 0.1, 0.1, new Vec3(1.1, 0.1, 0.1));
        particle.setParticleSpeed(0.3, 0.0, 0.0);

        particle.tick();

        assertEquals(1.2, particle.getPos().x, 1.0E-9);
        assertFalse(particle.isAlive());
        assertEquals(1, particle.updateCalls);
    }

    @Test
    void delayedBaseTickDoesNotAgeMoveOrUpdateSeeker() {
        CountingSeeker particle = new CountingSeeker(mock(ClientLevel.class), 0.0, 0.0, 0.0, new Vec3(10.0, 0.0, 0.0));
        particle.setParticleSpeed(0.3, 0.0, 0.0);
        particle.delay(2);

        particle.tick();

        assertEquals(0, particle.ageValue());
        assertEquals(new Vec3(0.0, 0.0, 0.0), particle.getPos());
        assertEquals(0, particle.updateCalls);
        assertTrue(particle.isAlive());
    }

    @Test
    void expiredBaseTickDoesNotMoveOrRunSubclassUpdate() {
        CountingSeeker particle = new CountingSeeker(mock(ClientLevel.class), 0.0, 0.0, 0.0, new Vec3(10.0, 0.0, 0.0));
        particle.setParticleSpeed(0.3, 0.0, 0.0);
        particle.setLifetime(0);

        particle.tick();

        assertEquals(1, particle.ageValue());
        assertEquals(new Vec3(0.0, 0.0, 0.0), particle.getPos());
        assertEquals(0, particle.updateCalls);
        assertFalse(particle.isAlive());
    }

    private static final class CountingSeeker extends SeekerParticle {
        private int updateCalls;

        private CountingSeeker(ClientLevel level, double x, double y, double z, Vec3 target) {
            super(level, x, y, z, (ParticleSheet) null, NO_ENTITY, target, Vec3.ZERO, 0.0F);
        }

        @Override
        protected void update() {
            this.updateCalls++;
        }

        private void delay(int ticks) {
            setDelay(ticks);
        }

        private int ageValue() {
            return this.age;
        }
    }
}
