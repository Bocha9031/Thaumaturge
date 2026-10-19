package com.leclowndu93150.thaumaturge.content.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.TTIds;
import java.util.List;
import org.junit.jupiter.api.Test;

class LegacyTaintedAnimalsTest {
    @Test
    void migratesEveryHistoricalTaintEntityId() {
        assertEquals(
                List.of(
                        TTIds.rl("taint_spider"),
                        TTIds.rl("taint_creeper"),
                        TTIds.rl("taint_cow"),
                        TTIds.rl("taint_pig"),
                        TTIds.rl("taint_chicken"),
                        TTIds.rl("taint_sheep"),
                        TTIds.rl("taint_villager")),
                LegacyTaintedAnimals.legacyIds());
    }
}
