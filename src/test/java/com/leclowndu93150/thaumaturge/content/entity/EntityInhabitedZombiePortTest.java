package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

final class EntityInhabitedZombiePortTest {
    @Test
    void killedEntityAllowsTheVanillaDeathLootBranch() {
        ServerLevel level = mock(ServerLevel.class);
        TestZombie zombie = new TestZombie(TTEntities.INHABITED_ZOMBIE.get(), level);
        Zombie victim = mock(Zombie.class);

        // LivingEntity#die gates its death event and dropAllDeathLoot on this callback returning true.
        assertTrue(zombie.killedEntity(level, victim));
    }

    @Test
    void deathConvertsToOneCrabAtTheZombieEyeWithMatchingHeadRotation() {
        ServerLevel level = mock(ServerLevel.class);
        TestZombie zombie = new TestZombie(TTEntities.INHABITED_ZOMBIE.get(), level);
        EntityEldritchCrab crab = mock(EntityEldritchCrab.class);
        zombie.crab = crab;
        zombie.setPos(4.0D, 5.0D, 6.0D);
        zombie.setYRot(15.0F);
        zombie.setXRot(25.0F);
        zombie.setYHeadRot(35.0F);

        zombie.finishDeath();
        zombie.finishDeath();

        assertEquals(1, zombie.conversionAttempts);
        assertEquals(1, zombie.puffAttempts);
        verify(crab).moveTo(4.0D, zombie.getEyeY(), 6.0D, 15.0F, 25.0F);
        verify(crab).setYHeadRot(35.0F);
        verify(crab).setHelm(true);
        verify(level).addFreshEntity(crab);
    }

    private static final class TestZombie extends EntityInhabitedZombie {
        private EntityEldritchCrab crab;
        private int conversionAttempts;
        private int puffAttempts;

        private TestZombie(EntityType<? extends EntityInhabitedZombie> type, Level level) {
            super(type, level);
        }

        @Override
        protected EntityEldritchCrab createConversionCrab(ServerLevel level) {
            this.conversionAttempts++;
            return this.crab;
        }

        @Override
        protected void puffOfSmoke(ServerLevel level) {
            this.puffAttempts++;
        }

        private void finishDeath() {
            this.tickDeath();
        }
    }
}
