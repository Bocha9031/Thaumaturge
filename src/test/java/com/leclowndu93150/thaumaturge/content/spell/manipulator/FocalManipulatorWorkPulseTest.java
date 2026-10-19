package com.leclowndu93150.thaumaturge.content.spell.manipulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class FocalManipulatorWorkPulseTest {
    @Test
    void firstVisDrainOccursOnlyAfterAFullWorkInterval() {
        int pulse = 0;

        for (int tick = 1; tick < 20; tick++) {
            pulse = BlockEntityFocalManipulator.advanceWorkPulse(pulse);
            assertNotEquals(0, pulse, "must not draw vis before the 20th tick");
        }

        assertEquals(0, BlockEntityFocalManipulator.advanceWorkPulse(pulse));
    }

    @Test
    void workPulseWrapsForEachSubsequentInterval() {
        int pulse = 0;
        for (int interval = 0; interval < 2; interval++) {
            for (int tick = 0; tick < 20; tick++) {
                pulse = BlockEntityFocalManipulator.advanceWorkPulse(pulse);
            }
            assertEquals(0, pulse);
        }
    }
}
