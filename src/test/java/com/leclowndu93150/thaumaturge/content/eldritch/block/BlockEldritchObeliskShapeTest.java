package com.leclowndu93150.thaumaturge.content.eldritch.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;

class BlockEldritchObeliskShapeTest {
    @Test
    void eachPickablePartHasAnOutlineWithinItsOwnBlockCell() {
        for (int part = 0; part < BlockEldritchObelisk.PART_COUNT; part++) {
            var bounds = BlockEldritchObelisk.interactionShape(part).bounds();

            assertTrue(bounds.minY >= 0.0, "part " + part + " begins below its own cell");
            assertTrue(bounds.maxY <= 1.0, "part " + part + " extends beyond its own cell");
            assertFalse(BlockEldritchObelisk.interactionShape(part).isEmpty());
        }

        var base = BlockEldritchObelisk.interactionShape(0).toAabbs();
        assertTrue(base.stream().anyMatch(part -> part.contains(new Vec3(0.5, 0.125, 0.5))));
        assertFalse(base.stream().anyMatch(part -> part.contains(new Vec3(0.02, 0.125, 0.5))));

        var shaft = BlockEldritchObelisk.interactionShape(2).toAabbs();
        assertTrue(shaft.stream().anyMatch(part -> part.contains(new Vec3(0.5, 0.5, 0.01))));
        assertFalse(shaft.stream().anyMatch(part -> part.contains(new Vec3(0.5, 0.5, 0.5))));

        var tip = BlockEldritchObelisk.interactionShape(5).bounds();
        assertEquals(0.0, tip.minY);
        assertEquals(0.25, tip.maxY);
        assertEquals(5.25, 5 + tip.maxY);
    }

    @Test
    void everyVerticalPartHasSolidCollision() {
        for (int part = 0; part < BlockEldritchObelisk.PART_COUNT; part++) {
            assertEquals(Shapes.block(), BlockEldritchObelisk.collisionShape(part), "part " + part + " should block");
        }
    }
}
