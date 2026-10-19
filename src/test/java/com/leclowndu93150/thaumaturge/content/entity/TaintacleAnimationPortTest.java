package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

class TaintacleAnimationPortTest {
    @Test
    void strikeFlailEasesBackToRestAndNeverDropsBelowIt() {
        EntityTaintacle taintacle = new EntityTaintacle(TTEntities.TAINTACLE.get(), mock(ServerLevel.class));

        taintacle.startStrikeAnimation();
        taintacle.tickStrikeAnimation();
        assertEquals(2.954, taintacle.flailIntensity, 0.001);

        for (int tick = 0; tick < 300; tick++) {
            taintacle.tickStrikeAnimation();
        }
        assertEquals(1.0, taintacle.flailIntensity, 0.0);

        taintacle.tickStrikeAnimation();
        assertEquals(1.0, taintacle.flailIntensity, 0.0);
    }
}
